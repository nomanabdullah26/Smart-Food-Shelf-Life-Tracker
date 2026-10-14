package com.example.foodtracker.service;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = users.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new User(
                u.getEmail(),
                u.getPasswordHash(),
                Arrays.stream(u.getRoles().split(","))
                      .map(String::trim)
                      .map(SimpleGrantedAuthority::new)
                      .collect(Collectors.toList())
        );
    }
}
