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

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="HESAPLAB_ORACLE_TEST", matches="true")
class TransferOracleTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired TransferService service;
    @Autowired MockMvc mvc;
    @MockitoSpyBean TransferRepository repository;
    String username, owner, foreignOwner, source, target, foreign;
    List<Map<String,Object>> usersBefore, accountsBefore, movementsBefore;
    @BeforeEach void setup() {
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
        reset(repository);
        jdbc.update("DELETE FROM account_movements WHERE account_id IN (SELECT id FROM accounts WHERE owner_user_id IN (?,?))",owner,foreignOwner);
        jdbc.update("DELETE FROM accounts WHERE owner_user_id IN (?,?)",owner,foreignOwner);
        jdbc.update("DELETE FROM app_users WHERE id IN (?,?)",owner,foreignOwner);
        assertThat(jdbc.queryForList("SELECT * FROM app_users ORDER BY id")).isEqualTo(usersBefore);
        assertThat(jdbc.queryForList("SELECT * FROM accounts ORDER BY id")).isEqualTo(accountsBefore);
        assertThat(jdbc.queryForList("SELECT * FROM account_movements ORDER BY id")).isEqualTo(movementsBefore);
    }
    TransferRequest request(String amount) { return new TransferRequest(source,target,amount); }
    BigDecimal balance(String id) { return jdbc.queryForObject("SELECT balance FROM accounts WHERE id=?",BigDecimal.class,new BigDecimal(id)); }
    int movements() { return jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE account_id IN (?,?)",Integer.class,source,target); }
    @Test void commitsTwoBalancesAndTwoMovements() {
        service.transfer(owner,request("25.50"));
        assertThat(balance(source)).isEqualByComparingTo("74.50");
        assertThat(balance(target)).isEqualByComparingTo("35.50");
        assertThat(balance(source).add(balance(target))).isEqualByComparingTo("110.00");
        assertThat(movements()).isEqualTo(2);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM account_movements WHERE (account_id=? AND direction='DEBIT' AND balance_after=74.5) OR (account_id=? AND direction='CREDIT' AND balance_after=35.5)",Integer.class,source,target)).isEqualTo(2);
    }
    @Test void rejectsInvalidAmountsAndAccountsWithoutWrites() {
        for(String amount: List.of("0","-1","abc","1e2","1,2","1.001","10000000000000","101")) {
            assertThatThrownBy(()->service.transfer(owner,request(amount))).isInstanceOf(TransferService.Rejected.class);
        }
        for(String id: List.of(source,"000"+source,foreign,"999999999999999999","bad")) {
            assertThatThrownBy(()->service.transfer(owner,new TransferRequest(source,id,"1"))).isInstanceOf(TransferService.Rejected.class);
        }
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(movements()).isZero();
    }
    @Test void overflowRejectedWithoutRounding() {
        jdbc.update("UPDATE accounts SET balance=9999999999999.99 WHERE id=?",target);
        assertThatThrownBy(()->service.transfer(owner,request("0.01"))).isInstanceOf(TransferService.Rejected.class);
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(movements()).isZero();
    }
    @Test void realOracleWriteFailureRollsBackEarlierWrites() {
        // Yalnız test spy'ı ikinci INSERT'i gerçek Oracle check hatasına çevirir.
        doAnswer(call -> { jdbc.update("INSERT INTO account_movements (account_id,direction,amount,balance_after) VALUES (?, 'INVALID', 1, 1)",target); return null; })
                .when(repository).movement(eq(new BigDecimal(target)),eq("CREDIT"),any(),any());
        assertThatThrownBy(()->service.transfer(owner,request("25"))).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThat(balance(source)).isEqualByComparingTo("100"); assertThat(balance(target)).isEqualByComparingTo("10"); assertThat(movements()).isZero();
    }
    @Test void parallelTransfersCannotOverdrawOrLoseUpdates() throws Exception {
        var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> transfer=()->{try {service.transfer(owner,request("80"));return true;}catch(TransferService.Rejected error){return false;}};
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
                mvc.perform(post("/api/transfers").with(user(customer)).with(csrf()).contentType("application/json")
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
        mvc.perform(post("/api/transfers").with(csrf()).contentType("application/json").content(body)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/transfers").with(user(admin)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").with(user(customer)).contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/transfers").with(user(customer)).with(csrf()).contentType("application/json").content(body.replace("\""+target+"\"","\""+foreign+"\""))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/transfers").with(user(customer)).with(csrf()).contentType("application/json").content(body.replace("\"1.00\"","1.00"))).andExpect(status().isBadRequest());
        assertThat(movements()).isZero(); assertThat(balance(source)).isEqualByComparingTo("100");
        mvc.perform(post("/api/transfers").with(user(customer)).with(csrf()).contentType("application/json").content(body)).andExpect(status().isOk());
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
        service.transfer(owner,request("25.50"));
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
        mvc.perform(post("/api/transfers").with(user(admin)).with(csrf()).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }}
