package com.marketplace.repository;

import com.marketplace.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
List<Product> findBySellerId(Long sellerId);
}
