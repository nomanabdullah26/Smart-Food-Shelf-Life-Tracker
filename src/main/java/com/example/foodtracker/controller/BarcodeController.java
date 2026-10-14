package com.example.foodtracker.controller;

import com.example.foodtracker.service.BarcodeService;
import com.example.foodtracker.service.BarcodeService.ProductInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/barcode")
public class BarcodeController {

    private final BarcodeService barcodeService;

    public BarcodeController(BarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam("payload") String payload) {
        ProductInfo info = barcodeService.lookup(payload);
        if (info == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(info);
    }
}
