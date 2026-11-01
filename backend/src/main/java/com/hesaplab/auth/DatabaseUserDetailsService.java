package com.hesaplab.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository repository;
    public DatabaseUserDetailsService(UserRepository repository) { this.repository = repository; }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repository.findByUsername(username).filter(user -> user.getPassword() != null)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı adı veya parola hatalı."));
    }
}
