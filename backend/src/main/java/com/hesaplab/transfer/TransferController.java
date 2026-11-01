package com.hesaplab.transfer;

import com.hesaplab.auth.AppUserDetails;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService service;
    public TransferController(TransferService service) { this.service = service; }
    @PostMapping
    public Map<String, String> transfer(@AuthenticationPrincipal AppUserDetails user, @RequestBody TransferRequest request) {
        service.transfer(user.user().id(), request);
        return Map.of("message", "Transfer tamamlandı.");
    }
}
