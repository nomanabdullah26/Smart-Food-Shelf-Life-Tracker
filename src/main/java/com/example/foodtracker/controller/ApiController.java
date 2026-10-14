package com.example.foodtracker.controller;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.repository.FoodItemRepository;
import com.example.foodtracker.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final FoodItemRepository foodRepo;
    private final UserService userService;

    public ApiController(FoodItemRepository foodRepo, UserService userService) {
        this.foodRepo = foodRepo;
        this.userService = userService;
    }

    @GetMapping("/sensors")
    public Map<String,Object> sensors(Authentication auth) {
        // Simulate temperature/humidity + compute near expiry from DB
        AppUser owner = userService.findByEmail(auth.getName());
        LocalDate today = LocalDate.now();
        long nearExpiry = foodRepo.countByOwnerAndExpiryDateBetween(owner, today, today.plusDays(3));

        Map<String,Object> m = new HashMap<>();
        m.put("temperature", Math.round((Math.random()*10+2)*10.0)/10.0);
        m.put("humidity", (int)(Math.random()*30+50));
        m.put("nearExpiry", nearExpiry);
        return m;
    }

    @GetMapping("/reports/waste")
    public Map<String,Object> wasteReport() {
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("Jan", "Feb", "Mar", "Apr"));
        data.put("values", Arrays.asList(50, 30, 20, 10));
        return data;
    }
}
