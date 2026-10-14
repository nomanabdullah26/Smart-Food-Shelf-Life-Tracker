package com.example.foodtracker.repository;

import com.example.foodtracker.entity.ProductCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCatalogRepository extends JpaRepository<ProductCatalog, Long> {
    Optional<ProductCatalog> findByBarcode(String barcode);
}
