package com.hesaplab.account;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountService {
    public static class AccountNotFoundException extends RuntimeException { }
    private final AccountRepository repository;
    public AccountService(AccountRepository repository) { this.repository = repository; }
    public List<AccountDto> getAccounts(String ownerId) { return repository.findByOwner(ownerId); }
    public AccountDto getAccount(String accountId, String ownerId) {
        if (!accountId.matches("[0-9]{1,38}")) { throw new AccountNotFoundException(); }
        return repository.findOwnedAccount(accountId, ownerId).orElseThrow(AccountNotFoundException::new);
    }
}
