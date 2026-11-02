package com.hesaplab.receipt;

import com.hesaplab.auth.AppUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers/{transferId}/receipt")
public class ReceiptController {
    private final ReceiptService service;
    public ReceiptController(ReceiptService service) { this.service = service; }

    @GetMapping
    public ReceiptRepository.Receipt get(@AuthenticationPrincipal AppUserDetails user, @PathVariable String transferId) {
        return service.get(user.user().id(), transferId);
    }

    @PostMapping
    public ReceiptRepository.Receipt request(@AuthenticationPrincipal AppUserDetails user, @PathVariable String transferId) {
        return service.requestAgain(user.user().id(), transferId);
    }
}
