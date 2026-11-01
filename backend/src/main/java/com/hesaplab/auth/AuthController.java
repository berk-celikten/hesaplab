package com.hesaplab.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record CsrfDto(String token, String headerName) { }
    @GetMapping("/csrf")
    public CsrfDto csrf(CsrfToken token) {
        return new CsrfDto(token.getToken(), token.getHeaderName());
    }
    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AppUserDetails principal) { return principal.user(); }
}
