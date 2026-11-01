package com.hesaplab.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hesaplab.account.AccountController;
import com.hesaplab.account.AccountDto;
import com.hesaplab.account.AccountRepository;
import com.hesaplab.account.AccountService;
import com.hesaplab.account.ApiExceptionHandler;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, AccountController.class}, properties = "spring.config.import=")
@Import({SecurityConfiguration.class, PasswordConfiguration.class, DatabaseUserDetailsService.class,
        AccountService.class, ApiExceptionHandler.class})
class SecurityApiTest {
    // Yalnız bu testin sahte parolasıdır; yerel demo giriş parolası değildir.
    private static final String TEST_PASSWORD = "unit-test-only-password";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired PasswordEncoder encoder;
    @MockitoBean UserRepository users;
    @MockitoBean AccountRepository accounts;

    private record CsrfSession(MockHttpSession session, String token, String header) { }

    @BeforeEach
    void fixtures() {
        String hash = encoder.encode(TEST_PASSWORD);
        when(users.findByUsername("customer1")).thenReturn(Optional.of(
                new AppUserDetails(new UserDto("17", "customer1", "Müşteri 1", "CUSTOMER"), hash)));
        when(users.findByUsername("customer2")).thenReturn(Optional.of(
                new AppUserDetails(new UserDto("29", "customer2", "Müşteri 2", "CUSTOMER"), hash)));
        when(users.findByUsername("admin")).thenReturn(Optional.of(
                new AppUserDetails(new UserDto("41", "admin", "Yönetici", "ADMIN"), hash)));
        when(accounts.findByOwner("17")).thenReturn(List.of(
                new AccountDto("101", "17", "Vadesiz", "10000.00", "TRY"),
                new AccountDto("102", "17", "Birikim", "3500.00", "TRY")));
        when(accounts.findByOwner("29")).thenReturn(List.of(
                new AccountDto("201", "29", "Vadesiz", "2000.00", "TRY")));
    }

    private CsrfSession csrf(MockHttpSession session) throws Exception {
        MvcResult result = mvc.perform(get("/api/auth/csrf").session(session))
                .andExpect(status().isOk()).andReturn();
        var data = mapper.readTree(result.getResponse().getContentAsString());
        return new CsrfSession((MockHttpSession) result.getRequest().getSession(),
                data.get("token").asText(), data.get("headerName").asText());
    }

    private MockHttpSession login(String username) throws Exception {
        CsrfSession token = csrf(new MockHttpSession());
        MvcResult result = mvc.perform(post("/api/auth/login").session(token.session())
                .header(token.header(), token.token()).param("username", username).param("password", TEST_PASSWORD))
                .andExpect(status().isOk()).andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    void anonymousRequestsReturnJson401() throws Exception {
        for (String path : List.of("/api/auth/me", "/api/accounts", "/api/accounts/101")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(header().doesNotExist("Location"));
        }
    }

    @Test
    void loginAndLogoutRequireCsrf() throws Exception {
        mvc.perform(post("/api/auth/login").param("username", "customer1").param("password", TEST_PASSWORD))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/logout").session(login("customer1")))
                .andExpect(status().isForbidden());
    }

    @Test
    void wrongUserAndWrongPasswordHaveIdenticalResponses() throws Exception {
        CsrfSession token = csrf(new MockHttpSession());
        String first = mvc.perform(post("/api/auth/login").session(token.session())
                .header(token.header(), token.token()).param("username", "unknown").param("password", TEST_PASSWORD))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String second = mvc.perform(post("/api/auth/login").session(token.session())
                .header(token.header(), token.token()).param("username", "customer1").param("password", "incorrect"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void loginChangesSessionAndExposesOnlySafeSummary() throws Exception {
        CsrfSession token = csrf(new MockHttpSession());
        String previousId = token.session().getId();
        mvc.perform(post("/api/auth/login").session(token.session()).header(token.header(), token.token())
                .param("username", "customer1").param("password", TEST_PASSWORD))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("17"))
                .andExpect(jsonPath("$.username").value("customer1"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.session").doesNotExist());
        assertThat(token.session().getId()).isNotEqualTo(previousId);
        mvc.perform(get("/api/auth/me").session(token.session())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("17"));
        // Giriş öncesindeki token, yeni oturumun POST isteğinde artık geçerli değildir.
        mvc.perform(post("/api/auth/logout").session(token.session()).header(token.header(), token.token()))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedOwnerCannotBeOverriddenByParameters() throws Exception {
        mvc.perform(get("/api/accounts?customerId=29&ownerId=29").session(login("customer1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("17"))
                .andExpect(jsonPath("$[0].balance").isString());
        verify(accounts).findByOwner("17");
        mvc.perform(get("/api/accounts").session(login("customer2")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value("29"));
    }

    @Test
    void foreignMissingAndInvalidIdsUseSame404() throws Exception {
        MockHttpSession session = login("customer1");
        String response = null;
        for (String id : List.of("201", "999", "abc")) {
            String body = mvc.perform(get("/api/accounts/" + id).session(session))
                    .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
            if (response == null) response = body;
            assertThat(body).isEqualTo(response);
        }
        verify(accounts).findOwnedAccount("201", "17");
    }

    @Test
    void ownedDetailWorksAndAdminCannotUseCustomerEndpoints() throws Exception {
        when(accounts.findOwnedAccount("101", "17")).thenReturn(Optional.of(
                new AccountDto("101", "17", "Vadesiz", "10000.00", "TRY")));
        mvc.perform(get("/api/accounts/101").session(login("customer1")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value("10000.00"));
        MockHttpSession admin = login("admin");
        mvc.perform(get("/api/auth/me").session(admin)).andExpect(jsonPath("$.role").value("ADMIN"));
        mvc.perform(get("/api/accounts").session(admin)).andExpect(status().isForbidden());
        mvc.perform(get("/api/accounts/101").session(admin)).andExpect(status().isForbidden());
    }

    @Test
    void legacyDemoEndpointsNeverExposeBulkData() throws Exception {
        for (String username : List.of("customer1", "customer2", "admin")) {
            MockHttpSession session = login(username);
            for (String resource : List.of("customers", "accounts")) {
                mvc.perform(get("/api/demo/" + resource).session(session)).andExpect(status().isForbidden());
            }
        }
    }

    @Test
    void freshCsrfAllowsLogoutAndInvalidatesSession() throws Exception {
        MockHttpSession session = login("customer1");
        CsrfSession token = csrf(session);
        mvc.perform(post("/api/auth/logout").session(session).header(token.header(), token.token()))
                .andExpect(status().isOk()).andExpect(cookie().maxAge("HESAPLAB_SESSION", 0));
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        csrf(new MockHttpSession());
    }

    @Test
    void databaseFailureDoesNotExposeSql() throws Exception {
        when(accounts.findByOwner("17")).thenThrow(new DataAccessResourceFailureException("private SQL details"));
        mvc.perform(get("/api/accounts").session(login("customer1"))).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Veriler şu anda alınamıyor. Lütfen tekrar deneyin."))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
