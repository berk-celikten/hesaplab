package com.hesaplab.account;

import com.hesaplab.auth.AppUserDetails;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }
    @GetMapping
    public List<AccountDto> accounts(@AuthenticationPrincipal AppUserDetails principal) {
        return service.getAccounts(principal.user().id());
    }
    @GetMapping("/{id}")
    public AccountDto account(@PathVariable String id, @AuthenticationPrincipal AppUserDetails principal) {
        return service.getAccount(id, principal.user().id());
    }
}
