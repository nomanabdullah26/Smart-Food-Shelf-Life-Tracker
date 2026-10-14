package com.example.foodtracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/app")
    public String app(Authentication auth, Model model) {
        model.addAttribute("userEmail", auth.getName());
        return "app";
    }
}
