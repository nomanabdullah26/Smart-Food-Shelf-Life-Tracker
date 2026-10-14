package com.example.foodtracker.controller;

import com.example.foodtracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/")
    public String loginPage(Authentication auth,
                            Model model,
                            @RequestParam(value="error", required=false) String error,
                            @RequestParam(value="registered", required=false) String registered) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/app";
        }
        model.addAttribute("loginError", error != null);
        model.addAttribute("registered", registered != null);
        return "login";
    }

    @PostMapping("/register")
    public String register(@RequestParam @NotBlank String name,
                           @RequestParam @NotBlank @Email String email,
                           @RequestParam @NotBlank String password,
                           Model model) {
        try {
            userService.register(name, email, password);
            return "redirect:/?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "login";
        }
    }
}
