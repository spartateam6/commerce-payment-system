package io.github.spartateam6.commercepaymentsystem.domain.product.repository;

import io.github.spartateam6.commercepaymentsystem.domain.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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
}
