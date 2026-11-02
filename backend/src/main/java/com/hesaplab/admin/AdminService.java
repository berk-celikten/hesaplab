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
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;
    public AdminService(AdminRepository repository, org.springframework.security.crypto.password.PasswordEncoder encoder){this.repository=repository;this.encoder=encoder;}
    public List<UserDto> users(){return repository.users();}
    public List<AdminRepository.Account> accounts(){return repository.accounts();}
    @Transactional
    public String create(NewAccount request){
        if(request.ownerId()==null || !request.ownerId().matches("[0-9]{1,38}")) throw new InvalidAccount("Geçerli bir müşteri seçin.");
        String name=request.name()==null?"":request.name().strip();
        if(name.isEmpty() || name.codePointCount(0,name.length())>100) throw new InvalidAccount("Hesap adı 1–100 karakter olmalı.");
        return repository.create(new BigDecimal(request.ownerId()),name);
    }
    public record NewUser(String username, String name, String password) {
        @Override public String toString(){return "NewUser[redacted]";}
    }
    public record Rename(String name) { }
    private String validName(String value) {
        String name=value==null?"":value.strip();
        if(name.isEmpty() || name.codePointCount(0,name.length())>100) throw new InvalidAccount("Ad 1–100 karakter olmalı.");
        return name;
    }
    private BigDecimal validId(String value) {
        if(value==null || !value.matches("[0-9]{1,38}")) throw new InvalidAccount("Geçersiz kayıt.");
        return new BigDecimal(value);
    }
    @Transactional
    public UserDto createUser(NewUser request) {
        String username=request.username()==null?"":request.username().strip();
        if(!username.matches("[a-z0-9_]{3,50}")) throw new InvalidAccount("Kullanıcı adı 3–50 küçük ASCII harf, rakam veya alt çizgi olmalı.");
        String name=validName(request.name());
        String password=request.password();
        if(password==null || password.codePointCount(0,password.length())<15 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new InvalidAccount("Parola en az 15 karakter, en fazla 72 UTF-8 bayt olmalı.");
        return repository.createUser(username,name,encoder.encode(password));
    }
    @Transactional
    public void renameUser(String id, Rename request) {
        if(repository.renameUser(validId(id),validName(request.name()))!=1) throw new InvalidAccount("Düzenlenebilir müşteri bulunamadı.");
    }
    @Transactional
    public void renameAccount(String id, Rename request) {
        if(repository.renameAccount(validId(id),validName(request.name()))!=1) throw new InvalidAccount("Hesap bulunamadı.");
    }}
