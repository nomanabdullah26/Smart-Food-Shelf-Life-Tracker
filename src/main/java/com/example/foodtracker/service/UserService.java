package com.example.foodtracker.service;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser register(String name, String email, String rawPassword) {
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        String hash = passwordEncoder.encode(rawPassword);
        AppUser user = new AppUser(name, email, hash, "ROLE_USER");
        return users.save(user);
    }

    public AppUser findByEmail(String email) {
        return users.findByEmail(email).orElse(null);
    }
}
