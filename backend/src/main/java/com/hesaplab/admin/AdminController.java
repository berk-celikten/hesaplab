package com.hesaplab.admin;
import java.util.List;
import java.util.Map;
import com.hesaplab.auth.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService service;
    public AdminController(AdminService service){this.service=service;}
    @GetMapping("/users") public List<UserDto> users(){return service.users();}
    @GetMapping("/accounts") public List<AdminRepository.Account> accounts(){return service.accounts();}
    @PostMapping("/accounts") public ResponseEntity<Map<String,String>> create(@RequestBody AdminService.NewAccount request){
        return ResponseEntity.status(201).body(Map.of("id",service.create(request),"message","Hesap açıldı."));
    }
    @ExceptionHandler(AdminService.InvalidAccount.class)
    public ResponseEntity<Map<String,String>> invalid(AdminService.InvalidAccount error){return ResponseEntity.badRequest().body(Map.of("message",error.getMessage()));}
    @PostMapping("/users") public ResponseEntity<UserDto> createUser(@RequestBody AdminService.NewUser request) {
        return ResponseEntity.status(201).body(service.createUser(request));
    }
    @PatchMapping("/users/{id}") public Map<String,String> renameUser(@PathVariable String id,@RequestBody AdminService.Rename request) {
        service.renameUser(id,request); return Map.of("message","Müşteri adı güncellendi.");
    }
    @PatchMapping("/accounts/{id}") public Map<String,String> renameAccount(@PathVariable String id,@RequestBody AdminService.Rename request) {
        service.renameAccount(id,request); return Map.of("message","Hesap adı güncellendi.");
    }
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    public ResponseEntity<Map<String,String>> duplicate(){return ResponseEntity.status(409).body(Map.of("message","Bu kullanıcı adı kullanılıyor."));}}
