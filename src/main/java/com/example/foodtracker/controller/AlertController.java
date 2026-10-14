package com.example.foodtracker.controller;

import com.example.foodtracker.entity.Alert;
import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.service.AlertService;
import com.example.foodtracker.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final UserService userService;

    public AlertController(AlertService alertService, UserService userService) {
        this.alertService = alertService;
        this.userService = userService;
    }

    public static class AlertDTO {
        public Long id;
        public String type;
        public String message;
        public String createdAt;
        public String foodName;
        public String foodExpiry;
        public boolean dismissed;

        public static AlertDTO from(Alert a) {
            AlertDTO d = new AlertDTO();
            d.id = a.getId();
            d.type = a.getType();
            d.message = a.getMessage();
            d.createdAt = a.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            d.dismissed = a.isDismissed();
            if (a.getFoodItem() != null) {
                d.foodName = a.getFoodItem().getName();
                d.foodExpiry = a.getFoodItem().getExpiryDate() == null ? null
                        : a.getFoodItem().getExpiryDate().toString();
            }
            return d;
        }
    }

    @GetMapping
    public Map<String, Object> list(Authentication auth,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size,
                                    @RequestParam(defaultValue = "true") boolean onlyActive) {
        AppUser user = userService.findByEmail(auth.getName());
        Page<Alert> p = alertService.list(user, page, size, onlyActive);
        return Map.of(
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalPages", p.getTotalPages(),
                "totalElements", p.getTotalElements(),
                "content", p.getContent().stream().map(AlertDTO::from).toList()
        );
    }

    @PostMapping("/dismiss/{id}")
    public ResponseEntity<?> dismiss(Authentication auth, @PathVariable Long id) {
        AppUser user = userService.findByEmail(auth.getName());
        alertService.dismiss(id, user);
        return ResponseEntity.ok().build();
    }
}
