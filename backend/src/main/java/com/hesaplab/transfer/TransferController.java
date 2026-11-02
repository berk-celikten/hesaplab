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
    public TransferService.Result transfer(@AuthenticationPrincipal AppUserDetails user, @RequestHeader(value="Idempotency-Key",required=false) String key, @RequestBody TransferRequest request) {
        return service.transfer(user.user().id(), key, request);

    }
}
