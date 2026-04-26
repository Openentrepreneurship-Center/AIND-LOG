package com.backend.domain.product.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.product.entity.Product;
import com.backend.domain.product.exception.ProductNotFoundException;

public interface ProductRepository extends JpaRepository<Product, String>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Product> {

    Page<Product> findByStockQuantityGreaterThan(int stockQuantity, Pageable pageable);

    @Query(value = "SELECT * FROM products WHERE deleted_at IS NULL ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    List<Product> findRandomFeaturedProducts();

    default Product findByIdOrThrow(String id) {
        return findById(id)
                .orElseThrow(ProductNotFoundException::new);
    }

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity WHERE p.id = :id AND p.stockQuantity >= :quantity")
    int decreaseStockAtomically(@Param("id") String id, @Param("quantity") int quantity);

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity WHERE p.id = :id")
    int increaseStockAtomically(@Param("id") String id, @Param("quantity") int quantity);
}
