package com.hesaplab.transfer;

import com.hesaplab.auth.AppUserDetails;
import com.hesaplab.auth.UserDto;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties="hesaplab.outbox.enabled=false")
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="HESAPLAB_ORACLE_TEST", matches="true")
class TransferOracleTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TransferService service;
    @Autowired MockMvc mvc;
    @Autowired com.hesaplab.admin.AdminService adminService;
    @MockitoSpyBean TransferRepository repository;
    @org.springframework.test.context.bean.override.mockito.MockitoBean com.hesaplab.receipt.ReceiptPublisher publisher;
    @Autowired com.hesaplab.receipt.ReceiptService receipts;
    @Autowired com.hesaplab.receipt.OutboxRelay relay;
    @MockitoSpyBean com.hesaplab.receipt.OutboxRepository outbox;
    List<Map<String,Object>> outboxBefore;
    @MockitoSpyBean com.hesaplab.receipt.ReceiptRepository receiptRepository;
    List<Map<String,Object>> receiptsBefore;
    String username, owner, foreignOwner, source, target, foreign;
    List<Map<String,Object>> usersBefore, accountsBefore, movementsBefore, transfersBefore;
    @BeforeEach void setup() {
        outboxBefore = jdbc.queryForList("SELECT * FROM receipt_outbox ORDER BY event_id");
        receiptsBefore = jdbc.queryForList("SELECT id,transfer_id,status,TO_CHAR(content) AS content,error_message,created_at,updated_at FROM transfer_receipts ORDER BY id");
        transfersBefore = jdbc.queryForList("SELECT * FROM transfers ORDER BY id");
        usersBefore = jdbc.queryForList("SELECT * FROM app_users ORDER BY id");
        accountsBefore = jdbc.queryForList("SELECT * FROM accounts ORDER BY id");
        movementsBefore = jdbc.queryForList("SELECT * FROM account_movements ORDER BY id");
        username = "transfer_test_" + UUID.randomUUID().toString().substring(0,8);
        jdbc.update("INSERT INTO app_users (username, display_name, role) VALUES (?, 'İzole test', 'CUSTOMER')", username);
        jdbc.update("INSERT INTO app_users (username, display_name, role) VALUES (?, 'İzole test', 'CUSTOMER')", username+"_other");
        owner = jdbc.queryForObject("SELECT id FROM app_users WHERE username=?", String.class, username);
        foreignOwner = jdbc.queryForObject("SELECT id FROM app_users WHERE username=?", String.class, username+"_other");
        source = account(owner, "Source", "100.00"); target = account(owner, "Target", "10.00");
        foreign = account(foreignOwner, "Foreign", "20.00");
    }
    String account(String user, String name, String balance) {
        jdbc.update("INSERT INTO accounts (owner_user_id, name, balance) VALUES (?, ?, ?)",new BigDecimal(user),name,new BigDecimal(balance));
        return jdbc.queryForObject("SELECT id FROM accounts WHERE owner_user_id=? AND name=?",String.class,new BigDecimal(user),name);
    }
    @AfterEach void cleanup() {
        reset(repository, receiptRepository, outbox);
        jdbc.update("DELETE FROM receipt_outbox WHERE transfer_id IN (SELECT id FROM transfers WHERE user_id IN (?,?))",owner,foreignOwner);
        jdbc.update("DELETE FROM transfer_receipts WHERE transfer_id IN (SELECT id FROM transfers WHERE user_id IN (?,?))",owner,foreignOwner);
        jdbc.update("DELETE FROM account_movements WHERE account_id IN (SELECT id FROM accounts WHERE owner_user_id IN (?,?))",owner,foreignOwner);
        jdbc.update("DELETE FROM transfers WHERE user_id IN (?,?)",owner,foreignOwner);
        jdbc.update("DELETE FROM accounts WHERE owner_user_id IN (?,?)",owner,foreignOwner);
        jdbc.update("DELETE FROM app_users WHERE id IN (?,?)",owner,foreignOwner);
        assertThat(jdbc.queryForList("SELECT id,transfer_id,status,TO_CHAR(content) AS content,error_message,created_at,updated_at FROM transfer_receipts ORDER BY id")).isEqualTo(receiptsBefore);
        assertThat(jdbc.queryForList("SELECT * FROM receipt_outbox ORDER BY event_id")).isEqualTo(outboxBefore);
        assertThat(jdbc.queryForList("SELECT * FROM transfers ORDER BY id")).isEqualTo(transfersBefore);
        assertThat(jdbc.queryForList("SELECT * FROM app_users ORDER BY id")).isEqualTo(usersBefore);
        assertThat(jdbc.queryForList("SELECT * FROM accounts ORDER BY id")).isEqualTo(accountsBefore);
        assertThat(jdbc.queryForList("SELECT * FROM account_movements ORDER BY id")).isEqualTo(movementsBefore);
    }
    TransferRequest request(String amount) { return new TransferRequest(source,target,amount); }
    BigDecimal balance(String id) { return jdbc.queryForObject("SELECT balance FROM accounts WHERE id=?",BigDecimal.class,new BigDecimal(id)); }
    int movements() { return jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE account_id IN (?,?)",Integer.class,source,target); }
    @Test void commitsTwoBalancesAndTwoMovements() {
        service.transfer(owner,UUID.randomUUID().toString(),request("25.50"));
        assertThat(balance(source)).isEqualByComparingTo("74.50");
        assertThat(balance(target)).isEqualByComparingTo("35.50");
        assertThat(balance(source).add(balance(target))).isEqualByComparingTo("110.00");
        assertThat(movements()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE (account_id=? AND direction='DEBIT' AND balance_after=74.5) OR (account_id=? AND direction='CREDIT' AND balance_after=35.5)",Integer.class,source,target)).isEqualTo(2);
    }
    @Test void rejectsInvalidAmountsAndAccountsWithoutWrites() {
        for(String amount: List.of("0","-1","abc","1e2","1,2","1.001","10000000000000","101")) {
            assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),request(amount))).isInstanceOf(TransferService.Rejected.class);
        }
        for(String id: List.of(source,"000"+source,"999999999999999999","bad")) {
            assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),new TransferRequest(source,id,"1"))).isInstanceOf(TransferService.Rejected.class);
        }
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(movements()).isZero();
    }
    @Test void overflowRejectedWithoutRounding() {
        jdbc.update("UPDATE accounts SET balance=9999999999999.99 WHERE id=?",target);
        assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),request("0.01"))).isInstanceOf(TransferService.Rejected.class);
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(movements()).isZero();
    }
    @Test void realOracleWriteFailureRollsBackEarlierWrites() {
        // Yalnız test spy'ı ikinci INSERT'i gerçek Oracle check hatasına çevirir.
        doAnswer(call -> { jdbc.update("INSERT INTO account_movements (account_id,direction,amount,balance_after) VALUES (?, 'INVALID', 1, 1)",target); return null; })
                .when(repository).movement(eq(new BigDecimal(target)),eq("CREDIT"),any(),any(),any());
        assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),request("25"))).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(balance(target)).isEqualByComparingTo("10"); assertThat(movements()).isZero();
    }
    @Test void parallelTransfersCannotOverdrawOrLoseUpdates() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> transfer=()->{try {service.transfer(owner,UUID.randomUUID().toString(),request("80"));return true;}catch(TransferService.Rejected error){return false;}};
            var results=pool.invokeAll(List.of(transfer,transfer));
            assertThat(results.stream().filter(f->{try{return f.get();}catch(Exception e){throw new RuntimeException(e);}}).count()).isEqualTo(1);
            assertThat(balance(source)).isEqualByComparingTo("20"); assertThat(balance(target)).isEqualByComparingTo("90"); assertThat(movements()).isEqualTo(2);
        } finally {pool.shutdownNow();}
    }
    @Test void boundedLockWaitReturnsConflict() throws Exception {
        try (var connection = jdbc.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var statement = connection.prepareStatement("SELECT id FROM accounts WHERE id=? FOR UPDATE")) {
                    statement.setBigDecimal(1,new BigDecimal(source)); statement.executeQuery().close();
                }
                var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
                long start=System.nanoTime();
                mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(customer)).with(csrf()).contentType("application/json")
                    .content("{\"sourceAccountId\":\""+source+"\",\"targetAccountId\":\""+target+"\",\"amount\":\"1\"}"))
                    .andExpect(status().isConflict());
                assertThat(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()-start)).isBetween(4L,10L);
            } finally { connection.rollback(); }
        }
        assertThat(movements()).isZero();
    }
    @Test void apiEnforcesSessionRoleCsrfAndOwnership() throws Exception {
        String body="{\"sourceAccountId\":\""+source+"\",\"targetAccountId\":\""+target+"\",\"amount\":\"1.00\"}";
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(admin)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(customer)).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(customer)).with(csrf()).contentType("application/json").content(body.replace("\""+source+"\"","\""+foreign+"\""))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(customer)).with(csrf()).contentType("application/json").content(body.replace("\"1.00\"","1.00"))).andExpect(status().isBadRequest());
        assertThat(movements()).isZero(); assertThat(balance(source)).isEqualByComparingTo("100");
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk());
        assertThat(movements()).isEqualTo(2);
    }
    @Test void movementsOwnershipEmptyTransferAndLimit() throws Exception {
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
        String url="/api/accounts/"+source+"/movements";
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).with(user(admin))).andExpect(status().isForbidden());
        for(String id: List.of(foreign,"bad","999999999999999999")) {
            mvc.perform(get("/api/accounts/"+id+"/movements").with(user(customer)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Hesap bulunamadı."));
        }
        mvc.perform(get(url).with(user(customer))).andExpect(status().isOk()).andExpect(content().json("[]"));
        service.transfer(owner,UUID.randomUUID().toString(),request("25.50"));
        mvc.perform(get(url).with(user(customer))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].direction").value("DEBIT")).andExpect(jsonPath("$[0].amount").value("25.50"))
            .andExpect(jsonPath("$[0].balanceAfter").value("74.50"))
            .andExpect(jsonPath("$[0].createdAt").value(org.hamcrest.Matchers.matchesPattern(".*(Z|[+-][0-9]{2}:[0-9]{2})$")));
        mvc.perform(get("/api/accounts/"+target+"/movements").with(user(customer))).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].direction").value("CREDIT")).andExpect(jsonPath("$[0].balanceAfter").value("35.50"));
        for(int n=0;n<55;n++) jdbc.update("INSERT INTO account_movements(account_id,direction,amount,balance_after,description,created_at) VALUES (?,'CREDIT',1,1,?,TO_TIMESTAMP_TZ('2099-01-01 12:00:00 +03:00','YYYY-MM-DD HH24:MI:SS TZH:TZM'))",source,"Order "+n);
        mvc.perform(get(url).with(user(customer))).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(50)).andExpect(jsonPath("$[0].description").value("Order 54"))
            .andExpect(jsonPath("$[49].description").value("Order 5"));
        assertThat(balance(source)).isEqualByComparingTo("74.50");
    }    @Test void adminAccountCreationAndAccess() throws Exception {
        var admin=new AppUserDetails(new UserDto(owner,username,"Admin test","ADMIN"),"");
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        var other=new AppUserDetails(new UserDto(foreignOwner,username+"_other","Other","CUSTOMER"),"");
        for(String url:List.of("/api/admin/users","/api/admin/accounts")) {
            mvc.perform(get(url)).andExpect(status().isUnauthorized());
            mvc.perform(get(url).with(user(customer))).andExpect(status().isForbidden());
            String response=mvc.perform(get(url).with(user(admin))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(response).doesNotContain("password","hash","session");
        }
        String body="{\"ownerId\":\""+owner+"\",\"name\":\"New isolated account\",\"balance\":999,\"role\":\"ADMIN\"}";
        mvc.perform(post("/api/admin/accounts").with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/accounts").with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/accounts").with(user(admin)).contentType("application/json").content(body)).andExpect(status().isForbidden());
        int before=jdbc.queryForObject("SELECT COUNT(*) FROM accounts",Integer.class);
        String adminId=jdbc.queryForObject("SELECT id FROM app_users WHERE username='demo_admin'",String.class);
        for(String invalid: List.of("{\"ownerId\":\""+adminId+"\",\"name\":\"No\"}",
            "{\"ownerId\":\"bad\",\"name\":\"No\"}","{\"ownerId\":\"999999999999999999\",\"name\":\"No\"}",
            "{\"ownerId\":\""+owner+"\",\"name\":\"   \"}","{\"ownerId\":\""+owner+"\",\"name\":\""+"X".repeat(101)+"\"}")) {
            mvc.perform(post("/api/admin/accounts").with(user(admin)).with(csrf()).contentType("application/json").content(invalid)).andExpect(status().isBadRequest());
        }
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounts",Integer.class)).isEqualTo(before);
        String response=mvc.perform(post("/api/admin/accounts").with(user(admin)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id=new com.fasterxml.jackson.databind.ObjectMapper().readTree(response).get("id").asText();
        mvc.perform(get("/api/accounts/"+id).with(user(customer))).andExpect(status().isOk()).andExpect(jsonPath("$.balance").value("0.00")).andExpect(jsonPath("$.currency").value("TRY"));
        mvc.perform(get("/api/accounts/"+id+"/movements").with(user(customer))).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/accounts/"+id).with(user(other))).andExpect(status().isNotFound());
        mvc.perform(get("/api/accounts").with(user(admin))).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").header("Idempotency-Key",UUID.randomUUID().toString()).with(user(admin)).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }    @Test void createsCustomerAndRealLoginThenRenamesWithoutOtherChanges() throws Exception {
        String login=username+"_new";
        String password=" " + UUID.randomUUID() + " "; // Yalnız izole testin geçici parolası.
        try {
            var customer=adminService.createUser(new com.hesaplab.admin.AdminService.NewUser(" "+login+" "," New name ",password));
            assertThat(customer.role()).isEqualTo("CUSTOMER");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounts WHERE owner_user_id=?",Integer.class,customer.id())).isZero();
            String hash=jdbc.queryForObject("SELECT password_hash FROM app_users WHERE id=?",String.class,customer.id());
            assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(password,hash)).isTrue();
            var result=mvc.perform(post("/api/auth/login").with(csrf()).param("username",login).param("password",password)).andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(200);
            assertThat(result.getResponse().getContentAsString()).doesNotContain("password","hash",password);
            var session=(org.springframework.mock.web.MockHttpSession)result.getRequest().getSession();
            mvc.perform(get("/api/accounts").session(session)).andExpect(status().isOk()).andExpect(content().json("[]"));
            var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
            var beforeUser=jdbc.queryForMap("SELECT * FROM app_users WHERE id=?",customer.id());
            mvc.perform(patch("/api/admin/users/"+customer.id()).with(user(admin)).with(csrf()).contentType("application/json")
                .content("{\"name\":\"Changed\",\"username\":\"hacked\",\"role\":\"ADMIN\",\"password\":\"hacked\"}")).andExpect(status().isOk());
            var afterUser=jdbc.queryForMap("SELECT * FROM app_users WHERE id=?",customer.id());
            beforeUser.remove("DISPLAY_NAME");afterUser.remove("DISPLAY_NAME");
            assertThat(beforeUser.equals(afterUser)).isTrue();
            // Oturumdaki principal görünen adı, yeniden girişe kadar eski kalır.
            mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.name").value("New name"));
            var beforeAccount=jdbc.queryForMap("SELECT * FROM accounts WHERE id=?",source);
            var movements=jdbc.queryForList("SELECT * FROM account_movements ORDER BY id");
            mvc.perform(patch("/api/admin/accounts/"+source).with(user(admin)).with(csrf()).contentType("application/json")
                .content("{\"name\":\"Renamed\",\"ownerId\":\""+foreignOwner+"\",\"balance\":999,\"currency\":\"USD\"}")).andExpect(status().isOk());
            var afterAccount=jdbc.queryForMap("SELECT * FROM accounts WHERE id=?",source);
            beforeAccount.remove("NAME");afterAccount.remove("NAME");assertThat(afterAccount).isEqualTo(beforeAccount);
            assertThat(jdbc.queryForList("SELECT * FROM account_movements ORDER BY id")).isEqualTo(movements);
            session.invalidate();
        } finally {jdbc.update("DELETE FROM app_users WHERE username=?",login);}
    }
    @Test void customerValidationPasswordBoundariesAndConcurrentUsername() throws Exception {
        String login=username+"_limits";
        for(String password:List.of("x".repeat(14),"x".repeat(73),"é".repeat(37),"😀".repeat(19))) {
            assertThatThrownBy(()->adminService.createUser(new com.hesaplab.admin.AdminService.NewUser(login,"Test",password))).isInstanceOf(com.hesaplab.admin.AdminService.InvalidAccount.class);
        }
        for(String password:List.of("x".repeat(15),"x".repeat(72),"é".repeat(36),"😀".repeat(18))) {
            try {adminService.createUser(new com.hesaplab.admin.AdminService.NewUser(login,"Test",password));}
            finally {jdbc.update("DELETE FROM app_users WHERE username=?",login);}
        }
        for(String invalid:List.of("ab","X_name","a-b","üser","x".repeat(51))) {
            assertThatThrownBy(()->adminService.createUser(new com.hesaplab.admin.AdminService.NewUser(invalid,"Test","x".repeat(15)))).isInstanceOf(com.hesaplab.admin.AdminService.InvalidAccount.class);
        }
        for(String invalid:List.of(" ","x".repeat(101))) {
            assertThatThrownBy(()->adminService.createUser(new com.hesaplab.admin.AdminService.NewUser(login,invalid,"x".repeat(15)))).isInstanceOf(com.hesaplab.admin.AdminService.InvalidAccount.class);
        }
        var pool=Executors.newFixedThreadPool(2);
        try {
            var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
            String body=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of("username",login,"name","Concurrent","password",UUID.randomUUID().toString(),"role","ADMIN"));
            Callable<Integer> create=()->mvc.perform(post("/api/admin/users").with(user(admin)).with(csrf()).contentType("application/json").content(body)).andReturn().getResponse().getStatus();
            var results=pool.invokeAll(List.of(create,create));
            assertThat(List.of(results.get(0).get(),results.get(1).get())).containsExactlyInAnyOrder(201,409);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM app_users WHERE username=? AND role='CUSTOMER'",Integer.class,login)).isEqualTo(1);
        } finally {pool.shutdownNow();jdbc.update("DELETE FROM app_users WHERE username=?",login);}
    }
    @Test void adminWriteSecurityAndRenameValidation() throws Exception {
        var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        String body=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of("username",username+"_blocked","name","Test","password",UUID.randomUUID().toString()));
        mvc.perform(post("/api/admin/users").with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/users").with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/users").with(user(admin)).contentType("application/json").content(body)).andExpect(status().isForbidden());
        String adminId=jdbc.queryForObject("SELECT id FROM app_users WHERE username='demo_admin'",String.class);
        for(String path:List.of("/api/admin/users/"+owner,"/api/admin/accounts/"+source)) {
            mvc.perform(patch(path).with(csrf()).contentType("application/json").content("{\"name\":\"Name\"}")).andExpect(status().isUnauthorized());
            mvc.perform(patch(path).with(user(customer)).with(csrf()).contentType("application/json").content("{\"name\":\"Name\"}")).andExpect(status().isForbidden());
            mvc.perform(patch(path).with(user(admin)).contentType("application/json").content("{\"name\":\"Name\"}")).andExpect(status().isForbidden());
            for(String invalid:List.of(" ","x".repeat(101))) mvc.perform(patch(path).with(user(admin)).with(csrf()).contentType("application/json")
                .content(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of("name",invalid)))).andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/api/admin/users/"+adminId).with(user(admin)).with(csrf()).contentType("application/json").content("{\"name\":\"No\"}")).andExpect(status().isBadRequest());
    }    @Test void replayEquivalentLostResponseAndConflict() throws Exception {
        String key=UUID.randomUUID().toString();
        var first=service.transfer(owner,key,request("80"));
        var replay=service.transfer(owner,key,new TransferRequest("000"+source,"00"+target,"80.00"));
        assertThat(replay).isEqualTo(first); // İlk yanıt kaybolsa da aynı kimlik/sonuç.
        assertThat(balance(source)).isEqualByComparingTo("20");assertThat(movements()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE transfer_id=?",Integer.class,first.transferId())).isEqualTo(2);
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        String body=new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request("1"));
        mvc.perform(post("/api/transfers").header("Idempotency-Key",key).with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isConflict());
        mvc.perform(post("/api/transfers").with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/transfers").header("Idempotency-Key","bad").with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        assertThat(movements()).isEqualTo(2);
        // Gerçek UNIQUE hatasının dış transaction sınırında ele alınmasını zorla.
        doReturn(Optional.empty()).doCallRealMethod().when(repository).find(owner,key);
        assertThat(service.transfer(owner,key,request("80"))).isEqualTo(first);
        assertThat(movements()).isEqualTo(2);
    }
    @Test void simultaneousSameKeyHasOneTransfer() throws Exception {
        String key=UUID.randomUUID().toString();
        var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<TransferService.Result> call=()->service.transfer(owner,key,request("25.50"));
            var results=pool.invokeAll(List.of(call,call));
            assertThat(results.get(0).get()).isEqualTo(results.get(1).get());
            assertThat(movements()).isEqualTo(2);assertThat(balance(source)).isEqualByComparingTo("74.50");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void failedWriteRollsBackTransferAndKeyCanBeRetried() {
        String key=UUID.randomUUID().toString();
        doAnswer(call->{jdbc.update("INSERT INTO account_movements(account_id,direction,amount,balance_after) VALUES (?,'INVALID',1,1)",target);return null;})
            .when(repository).movement(eq(new BigDecimal(target)),eq("CREDIT"),any(),any(),any());
        assertThatThrownBy(()->service.transfer(owner,key,request("25.5"))).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isZero();
        assertThat(movements()).isZero();assertThat(balance(source)).isEqualByComparingTo("100");
        reset(repository, receiptRepository, outbox);
        jdbc.update("DELETE FROM receipt_outbox WHERE transfer_id IN (SELECT id FROM transfers WHERE user_id IN (?,?))",owner,foreignOwner);
        jdbc.update("DELETE FROM transfer_receipts WHERE transfer_id IN (SELECT id FROM transfers WHERE user_id IN (?,?))",owner,foreignOwner);
        var result=service.transfer(owner,key,request("25.50"));
        assertThat(result.transferId()).isNotBlank();assertThat(movements()).isEqualTo(2);
    }    @Test void crossCustomerTransferRecipientPrivacyAndReplay() throws Exception {
        var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
        var admin=new AppUserDetails(new UserDto(owner,username,"Test","ADMIN"),"");
        mvc.perform(get("/api/recipients/"+foreign)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/recipients/"+foreign).with(user(admin))).andExpect(status().isForbidden());
        var result=mvc.perform(get("/api/recipients/"+foreign).with(user(customer))).andExpect(status().isOk()).andReturn();
        var json=new com.fasterxml.jackson.databind.ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertThat(json.size()).isEqualTo(4);assertThat(json.get("id").asText()).isEqualTo(foreign);
        assertThat(json.has("balance")).isFalse();assertThat(json.has("username")).isFalse();
        String key=UUID.randomUUID().toString();
        var first=service.transfer(owner,key,new TransferRequest(source,foreign,"25.5"));
        assertThat(service.transfer(owner,key,new TransferRequest("00"+source,"00"+foreign,"25.50"))).isEqualTo(first);
        assertThat(balance(source)).isEqualByComparingTo("74.50");assertThat(balance(foreign)).isEqualByComparingTo("45.50");
        assertThat(balance(source).add(balance(foreign))).isEqualByComparingTo("120");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE transfer_id=?",Integer.class,first.transferId())).isEqualTo(2);
        assertThatThrownBy(()->service.transfer(owner,key,new TransferRequest(source,foreign,"1"))).isInstanceOf(TransferService.Conflict.class);
        assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),new TransferRequest(foreign,source,"1"))).isInstanceOf(TransferService.Rejected.class);
        mvc.perform(get("/api/accounts/"+foreign).with(user(customer))).andExpect(status().isNotFound());
        mvc.perform(get("/api/accounts/"+foreign+"/movements").with(user(customer))).andExpect(status().isNotFound());
    }
    @Test void adminRecipientAndInvalidTargetsRejectedWithoutWrites() throws Exception {
        String adminId=jdbc.queryForObject("SELECT id FROM app_users WHERE username='demo_admin'",String.class);
        String adminAccount=account(adminId,"Isolated admin target "+username,"0");
        try {
            var customer=new AppUserDetails(new UserDto(owner,username,"Test","CUSTOMER"),"");
            for(String id:List.of(adminAccount,"bad","999999999999999999")) {
                mvc.perform(get("/api/recipients/"+id).with(user(customer))).andExpect(status().isNotFound());
                assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),new TransferRequest(source,id,"1"))).isInstanceOf(TransferService.Rejected.class);
            }
            assertThat(balance(source)).isEqualByComparingTo("100");assertThat(movements()).isZero();
        } finally {jdbc.update("DELETE FROM accounts WHERE id=?",adminAccount);}
    }
    @Test void oppositeCrossCustomerTransfersUseConsistentAccountLockOrder() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            var start=new CountDownLatch(1);
            Callable<TransferService.Result> forward=()->{start.await();return service.transfer(owner,UUID.randomUUID().toString(),new TransferRequest(source,foreign,"5"));};
            Callable<TransferService.Result> backward=()->{start.await();return service.transfer(foreignOwner,UUID.randomUUID().toString(),new TransferRequest(foreign,source,"3"));};
            var a=pool.submit(forward);var b=pool.submit(backward);start.countDown();
            a.get(12,TimeUnit.SECONDS);b.get(12,TimeUnit.SECONDS);
            assertThat(balance(source)).isEqualByComparingTo("98");assertThat(balance(foreign)).isEqualByComparingTo("22");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id IN (?,?)",Integer.class,owner,foreignOwner)).isEqualTo(2);
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE account_id IN (?,?)",Integer.class,source,foreign)).isEqualTo(4);
        }finally{pool.shutdownNow();}
    }

    @Test void receiptIsCreatedAtomicallyAndPublishedOnlyByRelay() throws Exception {
        String key=UUID.randomUUID().toString();
        String id=service.transfer(owner,key,request("25.50")).transferId();
        assertThat(service.transfer(owner,key,request("25.5")).transferId()).isEqualTo(id);
        verifyNoInteractions(publisher);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM receipt_outbox WHERE transfer_id=?",Integer.class,id)).isEqualTo(1);
        assertThat(relay.dispatchOne()).isTrue();
        verify(publisher).publish(eq(id),anyString());
        assertThat(jdbc.queryForObject("SELECT status FROM receipt_outbox WHERE transfer_id=?",String.class,id)).isEqualTo("SENT");
        assertThat(receipts.get(owner,id).status()).isEqualTo("PENDING"); // SENT != READY
        assertThat(movements()).isEqualTo(2);
    }

    @Test void publishFailureLeavesDurableEventWithBackoffAndManualRequestCoalesces() throws Exception {
        doThrow(new org.springframework.amqp.AmqpConnectException(new java.net.ConnectException("test"))).when(publisher).publish(anyString(),anyString());
        String id=service.transfer(owner,UUID.randomUUID().toString(),request("25.50")).transferId();
        assertThat(balance(source)).isEqualByComparingTo("74.50");
        verifyNoInteractions(publisher);
        relay.dispatchOne();
        assertThat(receipts.get(owner,id).status()).isEqualTo("PENDING");
        assertThat(jdbc.queryForObject("SELECT attempts FROM receipt_outbox WHERE transfer_id=?",Integer.class,id)).isEqualTo(1);
        assertThat(relay.dispatchOne()).isFalse(); // Henüz yeni deneme zamanı gelmedi.
        receipts.requestAgain(owner,"000"+id);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM receipt_outbox WHERE transfer_id=?",Integer.class,id)).isEqualTo(1);
        reset(publisher);
        jdbc.update("UPDATE receipt_outbox SET next_attempt_at=SYSTIMESTAMP WHERE transfer_id=?",id);
        relay.dispatchOne();
        jdbc.update("UPDATE transfer_receipts SET status='FAILED',error_message='Synthetic failure' WHERE transfer_id=?",id);
        var pool=Executors.newFixedThreadPool(2);
        try {
            for(var future:pool.invokeAll(List.<Callable<Void>>of(()->{receipts.requestAgain(owner,id);return null;},()->{receipts.requestAgain(owner,id);return null;}))) future.get();
        } finally {pool.shutdownNow();}
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM receipt_outbox WHERE transfer_id=?",Integer.class,id)).isEqualTo(2);
        assertThat(movements()).isEqualTo(2);
    }

    @Test void outboxWriteErrorRollsBackEntireTransfer() throws Exception {
        doAnswer(call -> { call.callRealMethod(); jdbc.update("UPDATE receipt_outbox SET status='INVALID' WHERE transfer_id=?",call.getArgument(0,String.class));return null; })
            .when(outbox).enqueue(anyString());
        assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),request("25.50"))).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(balance(source)).isEqualByComparingTo("100");
        assertThat(balance(target)).isEqualByComparingTo("10");
        assertThat(movements()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isZero();
        verifyNoInteractions(publisher);
    }

    @Test void concurrentRelaysSkipClaimedRowOnSeparateConnections() throws Exception {
        service.transfer(owner,UUID.randomUUID().toString(),request("25.50"));
        var entered=new CountDownLatch(1); var release=new CountDownLatch(1);
        doAnswer(call -> {entered.countDown();if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException("test timeout");return null;}).when(publisher).publish(anyString(),anyString());
        var pool=Executors.newFixedThreadPool(2);
        try {
            var first=pool.submit(()->relay.dispatchOne());
            assertThat(entered.await(5,TimeUnit.SECONDS)).isTrue();
            assertThat(pool.submit(()->relay.dispatchOne()).get(5,TimeUnit.SECONDS)).isFalse();
            release.countDown();
            assertThat(first.get(5,TimeUnit.SECONDS)).isTrue();
            verify(publisher,times(1)).publish(anyString(),anyString());
        } finally {release.countDown();pool.shutdownNow();}
    }

    @Autowired org.springframework.amqp.core.AmqpAdmin amqpAdmin;
    @Autowired org.springframework.amqp.rabbit.core.RabbitTemplate rabbit;

    @Test void confirmedPublishThenRealOracleSentFailureReplaysSameEventId() throws Exception {
        assertThat(amqpAdmin.getQueueInfo("hesaplab.receipt.queue").getMessageCount()).isZero();
        doAnswer(call -> {new com.hesaplab.receipt.ReceiptPublisher(amqpAdmin,rabbit).publish(call.getArgument(0),call.getArgument(1));return null;})
            .when(publisher).publish(anyString(),anyString());
        String id=service.transfer(owner,UUID.randomUUID().toString(),request("25.50")).transferId();
        String event=jdbc.queryForObject("SELECT event_id FROM receipt_outbox WHERE transfer_id=?",String.class,id);
        doAnswer(call -> {jdbc.update("UPDATE receipt_outbox SET status='INVALID' WHERE event_id=?",event);return null;}).when(outbox).sent(any());
        assertThatThrownBy(()->relay.dispatchOne()).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(jdbc.queryForObject("SELECT status FROM receipt_outbox WHERE event_id=?",String.class,event)).isEqualTo("PENDING");
        reset(outbox);
        relay.dispatchOne();
        for(int n=0;n<2;n++) {
            var message=rabbit.receive("hesaplab.receipt.queue",5000);
            assertThat(message).isNotNull();
            assertThat(message.getMessageProperties().getMessageId()).isEqualTo(event);
            assertThat(new String(message.getBody(),java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo(id);
        }
        assertThat(jdbc.queryForObject("SELECT status FROM receipt_outbox WHERE event_id=?",String.class,event)).isEqualTo("SENT");
        assertThat(movements()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfer_receipts WHERE transfer_id=?",Integer.class,id)).isEqualTo(1);
    }

    @Test void realReceiptWriteErrorRollsBackTransferAndDoesNotPublish() throws Exception {
        doAnswer(call -> {
            call.callRealMethod();
            jdbc.update("UPDATE transfer_receipts SET status='INVALID' WHERE transfer_id=?",call.getArgument(0,String.class));
            return null;
        }).when(receiptRepository).create(anyString());
        assertThatThrownBy(()->service.transfer(owner,UUID.randomUUID().toString(),request("25.50"))).isInstanceOf(org.springframework.dao.DataAccessException.class);
        verify(publisher,never()).publish(anyString(),anyString());
        assertThat(balance(source)).isEqualByComparingTo("100");
        assertThat(balance(target)).isEqualByComparingTo("10");
        assertThat(movements()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfers WHERE user_id=?",Integer.class,owner)).isZero();
    }

    @Test void receiptsAreSenderOnlyAndRetryDoesNotTransferMoney() throws Exception {
        String id=service.transfer(owner,UUID.randomUUID().toString(),new TransferRequest(source,foreign,"25.50")).transferId();
        var sender=new AppUserDetails(new UserDto(owner,username,"Sender","CUSTOMER"),"");
        var receiver=new AppUserDetails(new UserDto(foreignOwner,username+"_other","Receiver","CUSTOMER"),"");
        var admin=new AppUserDetails(new UserDto(owner,username,"Admin","ADMIN"),"");
        String url="/api/transfers/"+id+"/receipt";
        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).with(user(admin))).andExpect(status().isForbidden());
        for(String hidden:List.of(id,"bad","999999999999999999")) {
            mvc.perform(get("/api/transfers/"+hidden+"/receipt").with(user(receiver))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dekont bulunamadı."));
        }
        mvc.perform(post(url).with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post(url).with(user(admin)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(url).with(user(sender))).andExpect(status().isForbidden());
        mvc.perform(get(url).with(user(sender))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING"));
        mvc.perform(post(url).with(user(sender)).with(csrf())).andExpect(status().isOk());
        mvc.perform(post(url).with(user(sender)).with(csrf())).andExpect(status().isOk());
        assertThat(balance(source)).isEqualByComparingTo("74.50");
        assertThat(balance(foreign)).isEqualByComparingTo("45.50");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transfer_receipts WHERE transfer_id=?",Integer.class,id)).isEqualTo(1);
        jdbc.update("UPDATE transfer_receipts SET status='READY',content='Synthetic receipt' WHERE transfer_id=?",id);
        clearInvocations(publisher);
        mvc.perform(post(url).with(user(sender)).with(csrf())).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("READY"));
        verify(publisher,never()).publish(anyString(),anyString());
        mvc.perform(get("/api/accounts/"+source+"/movements").with(user(sender))).andExpect(jsonPath("$[0].receiptTransferId").value(id));
        mvc.perform(get("/api/accounts/"+foreign+"/movements").with(user(receiver))).andExpect(jsonPath("$[0].receiptTransferId").isEmpty());
        String body=mvc.perform(get(url).with(user(sender))).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("balance","password","session","username","role");
    }

}
