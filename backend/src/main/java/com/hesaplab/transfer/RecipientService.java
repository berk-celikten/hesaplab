package com.hesaplab.transfer;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hesaplab.account.AccountService.AccountNotFoundException;
@Service
@Transactional(readOnly=true)
public class RecipientService {
    private final TransferRepository repository;
    public RecipientService(TransferRepository repository){this.repository=repository;}
    public TransferRepository.Recipient find(String id){
        if(!id.matches("[0-9]{1,38}")) throw new AccountNotFoundException();
        return repository.recipient(new BigDecimal(id)).orElseThrow(AccountNotFoundException::new);
    }
}
