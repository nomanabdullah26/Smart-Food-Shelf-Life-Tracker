package com.example.foodtracker.repository;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long>
{

    // --- existing ---
    List<FoodItem> findAllByOwnerOrderByExpiryDateAsc(AppUser owner);
    long countByOwnerAndExpiryDateBetween(AppUser owner, LocalDate from, LocalDate to);

    // --- added for AlertService scheduler ---
    List<FoodItem> findByExpiryDateBetween(LocalDate start, LocalDate end);
    List<FoodItem> findByExpiryDateBefore(LocalDate date);
}
