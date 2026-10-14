package com.example.foodtracker.controller;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.Device;
import com.example.foodtracker.repository.DeviceRepository;
import com.example.foodtracker.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceRepository deviceRepo;
    private final UserService userService;

    public DeviceController(DeviceRepository deviceRepo, UserService userService) {
        this.deviceRepo = deviceRepo;
        this.userService = userService;
    }

    @PostMapping("/connect")
    public ResponseEntity<Device> connect(Authentication auth,
                            @RequestParam @NotBlank String deviceType) {
        AppUser owner = userService.findByEmail(auth.getName());
        Device d = new Device(deviceType, owner);
        return ResponseEntity.ok(deviceRepo.save(d));
    }

    @GetMapping
    public List<Device> list(Authentication auth) {
        AppUser owner = userService.findByEmail(auth.getName());
        return deviceRepo.findAllByOwnerOrderByConnectedAtDesc(owner);
    }
}
