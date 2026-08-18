package io.github.spartateam6.commercepaymentsystem.domain.product.repository;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Product p
        WHERE p.id IN :productIds
        ORDER BY p.id ASC
        """)
    List<Product> findAllByIdInForUpdate(
            @Param("productIds") Set<Long> productIds
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity " + "WHERE p.id = :productId AND p.stock >= :quantity")

    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity " + "WHERE p.id = :productId")

    int increaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
