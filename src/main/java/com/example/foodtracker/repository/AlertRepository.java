package com.example.foodtracker.repository;

import com.example.foodtracker.entity.Alert;
import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByUserOrderByCreatedAtDesc(AppUser user, Pageable pageable);

    Page<Alert> findByUserAndDismissedFalseOrderByCreatedAtDesc(AppUser user, Pageable pageable);

    Optional<Alert> findFirstByUserAndFoodItemAndTypeAndDismissedFalse(AppUser user, FoodItem foodItem, String type);
}
