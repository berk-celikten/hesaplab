package com.hesaplab.admin;
import java.math.BigDecimal;
import java.util.List;
import com.hesaplab.auth.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly=true)
public class AdminService {
    public static class InvalidAccount extends RuntimeException { public InvalidAccount(String message){super(message);} }
    public record NewAccount(String ownerId, String name) { }
    private final AdminRepository repository;
    public AdminService(AdminRepository repository){this.repository=repository;}
    public List<UserDto> users(){return repository.users();}
    public List<AdminRepository.Account> accounts(){return repository.accounts();}
    @Transactional
    public String create(NewAccount request){
        if(request.ownerId()==null || !request.ownerId().matches("[0-9]{1,38}")) throw new InvalidAccount("Geçerli bir müşteri seçin.");
        String name=request.name()==null?"":request.name().strip();
        if(name.isEmpty() || name.codePointCount(0,name.length())>100) throw new InvalidAccount("Hesap adı 1–100 karakter olmalı.");
        return repository.create(new BigDecimal(request.ownerId()),name);
    }
}
