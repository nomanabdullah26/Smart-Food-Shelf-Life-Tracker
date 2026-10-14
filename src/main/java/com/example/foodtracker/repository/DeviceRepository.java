package com.example.foodtracker.repository;

import com.example.foodtracker.entity.AppUser;
import com.example.foodtracker.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findAllByOwnerOrderByConnectedAtDesc(AppUser owner);
}
