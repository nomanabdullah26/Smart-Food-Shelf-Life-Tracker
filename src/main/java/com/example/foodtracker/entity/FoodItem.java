package com.example.foodtracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "food_items")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String barcode;
    private LocalDate expiryDate;

    // NEW FIELDS
    private LocalDate entryDate;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false,
        foreignKey = @ForeignKey(name="fk_fooditem_user"))
    private AppUser owner;

    public FoodItem() {}

    public FoodItem(String name, String barcode, LocalDate expiryDate, LocalDate entryDate, String ingredients, AppUser owner) {
        this.name = name;
        this.barcode = barcode;
        this.expiryDate = expiryDate;
        this.entryDate = entryDate;
        this.ingredients = ingredients;
        this.owner = owner;
    }

    // Getters & setters
    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public String getBarcode(){ return barcode; }
    public void setBarcode(String barcode){ this.barcode = barcode; }

    public LocalDate getExpiryDate(){ return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate){ this.expiryDate = expiryDate; }

    public LocalDate getEntryDate(){ return entryDate; }
    public void setEntryDate(LocalDate entryDate){ this.entryDate = entryDate; }

    public String getIngredients(){ return ingredients; }
    public void setIngredients(String ingredients){ this.ingredients = ingredients; }

    public AppUser getOwner(){ return owner; }
    public void setOwner(AppUser owner){ this.owner = owner; }
}
