package com.example.foodtracker.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "product_catalog",
    uniqueConstraints = @UniqueConstraint(name = "uk_catalog_barcode", columnNames = "barcode")
)
public class ProductCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String barcode;           // EAN/UPC/GTIN

    private String name;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    private Integer shelfLifeDays;    // default shelf-life (days) for expiry auto-calc

    public ProductCatalog() {}

    public ProductCatalog(String barcode, String name, String ingredients, Integer shelfLifeDays)
    {
        this.barcode = barcode;
        this.name = name;
        this.ingredients = ingredients;
        this.shelfLifeDays = shelfLifeDays;
    }

    // Getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public Integer getShelfLifeDays() { return shelfLifeDays; }
    public void setShelfLifeDays(Integer shelfLifeDays) { this.shelfLifeDays = shelfLifeDays; }
}
