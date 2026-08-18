package io.github.spartateam6.commercepaymentsystem.domain.product.repository;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " + "WHERE p.id = :productId AND p.stock >= :quantity")

    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}