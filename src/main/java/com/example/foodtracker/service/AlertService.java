package com.example.foodtracker.service;

import com.example.foodtracker.entity.Alert;
import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.FoodItem;
import com.example.foodtracker.repository.AlertRepository;
import com.example.foodtracker.repository.FoodItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepo;
    private final FoodItemRepository foodRepo;

    public AlertService(AlertRepository alertRepo, FoodItemRepository foodRepo) {
        this.alertRepo = alertRepo;
        this.foodRepo = foodRepo;
    }

    public Page<Alert> list(AppUser user, int page, int size, boolean onlyActive) {
        var pageable = PageRequest.of(page, size);
        return onlyActive
                ? alertRepo.findByUserAndDismissedFalseOrderByCreatedAtDesc(user, pageable)
                : alertRepo.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public void dismiss(Long id, AppUser user) {
        var alert = alertRepo.findById(id).orElse(null);
        if (alert == null) return;
        if (!alert.getUser().getId().equals(user.getId())) return; // ownership check
        alert.setDismissed(true);
        alertRepo.save(alert);
    }

    // ------- Daily generator -------
    // প্রতিদিন ভোর ৩টায় (server local time)
    @Scheduled(cron = "0 0 3 * * *")
    public void generateDailyExpiryAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate soon = today.plusDays(3);

        // Expiring within 3 days (today..soon)
        List<FoodItem> near = foodRepo.findByExpiryDateBetween(today, soon);
        for (FoodItem fi : near) {
            long daysLeft = ChronoUnit.DAYS.between(today, fi.getExpiryDate());
            String msg = fi.getName() + " expires in " + daysLeft + " day(s) (on " + fi.getExpiryDate() + ")";
            createIfNotExists(fi, "critical", msg);
        }

        // Already expired (yesterday or earlier)
        List<FoodItem> expired = foodRepo.findByExpiryDateBefore(today);
        for (FoodItem fi : expired) {
            long daysAgo = Math.abs(ChronoUnit.DAYS.between(fi.getExpiryDate(), today));
            String msg = fi.getName() + " expired " + daysAgo + " day(s) ago (on " + fi.getExpiryDate() + ")";
            createIfNotExists(fi, "critical", msg);
        }
    }

    private void createIfNotExists(FoodItem fi, String type, String message) {
        if (fi.getOwner() == null) return;
        var exists = alertRepo.findFirstByUserAndFoodItemAndTypeAndDismissedFalse(fi.getOwner(), fi, type);
        if (exists.isPresent()) return; // already active alert exists
        alertRepo.save(new Alert(type, message, fi.getOwner(), fi));
    }
}
