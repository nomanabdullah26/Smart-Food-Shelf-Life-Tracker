package com.example.foodtracker.controller;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.FoodItem;
import com.example.foodtracker.entity.ProductCatalog;
import com.example.foodtracker.repository.FoodItemRepository;
import com.example.foodtracker.repository.ProductCatalogRepository;
import com.example.foodtracker.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/food")
public class FoodController {

    private final FoodItemRepository foodRepo;
    private final UserService userService;
    private final ProductCatalogRepository catalogRepo;

    public FoodController(FoodItemRepository foodRepo,
                          UserService userService,
                          ProductCatalogRepository catalogRepo) {
        this.foodRepo = foodRepo;
        this.userService = userService;
        this.catalogRepo = catalogRepo;
    }

    @PostMapping
    public ResponseEntity<?> add(Authentication auth,
                                 @RequestParam @NotBlank String name,
                                 @RequestParam(required = false) String barcode,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
                                 @RequestParam(required = false) String ingredients,
                                 @RequestParam(required = false, defaultValue = "false") boolean saveDefault,
                                 @RequestParam(required = false) Integer shelfLifeDays) {

        // If no expiry but shelf-life provided, compute from entry
        if (expiryDate == null && entryDate != null && shelfLifeDays != null) {
            expiryDate = entryDate.plusDays(shelfLifeDays);
        }

        AppUser owner = userService.findByEmail(auth.getName());
        FoodItem item = new FoodItem(name, barcode, expiryDate, entryDate, ingredients, owner);
        foodRepo.save(item);

        // Optionally remember defaults for this barcode
        if (saveDefault && barcode != null && !barcode.isBlank()) {
            ProductCatalog pc = catalogRepo.findByBarcode(barcode)
                .orElse(new ProductCatalog(barcode, null, null, null));
            if (name != null && !name.isBlank()) pc.setName(name);
            if (ingredients != null && !ingredients.isBlank()) pc.setIngredients(ingredients);
            if (shelfLifeDays != null) pc.setShelfLifeDays(shelfLifeDays);
            catalogRepo.save(pc);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<FoodItem> list(Authentication auth) {
        AppUser owner = userService.findByEmail(auth.getName());
        return foodRepo.findAllByOwnerOrderByExpiryDateAsc(owner);
    }
}
