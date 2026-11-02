package com.hesaplab.transfer;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/recipients")
public class RecipientController {
    private final RecipientService service;
    public RecipientController(RecipientService service){this.service=service;}
    @GetMapping("/{accountId}") public TransferRepository.Recipient find(@PathVariable String accountId){return service.find(accountId);}
}
