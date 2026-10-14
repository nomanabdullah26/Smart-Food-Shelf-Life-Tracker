package com.example.foodtracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceType; // "Temperature Sensor", etc.
    private LocalDateTime connectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false,
        foreignKey = @ForeignKey(name="fk_device_user"))
    private AppUser owner;

    public Device(){}

    public Device(String deviceType, AppUser owner) {
        this.deviceType = deviceType;
        this.owner = owner;
        this.connectedAt = LocalDateTime.now();
    }

    // Getters & setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public String getDeviceType(){ return deviceType; }
    public void setDeviceType(String deviceType){ this.deviceType = deviceType; }

    public LocalDateTime getConnectedAt(){ return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt){ this.connectedAt = connectedAt; }

    public AppUser getOwner(){ return owner; }
    public void setOwner(AppUser owner){ this.owner = owner; }
}
