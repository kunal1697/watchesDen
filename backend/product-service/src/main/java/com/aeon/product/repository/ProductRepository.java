package com.aeon.product.repository;

import com.aeon.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query("select distinct p.collection from Product p where p.active = true order by p.collection")
    List<String> findActiveCollections();

    /**
     * Atomic, race-free stock decrement: the WHERE clause guarantees stock never goes negative even
     * when many checkouts hit the same product concurrently. Returns 1 on success, 0 if not enough stock.
     */
    @Modifying
    @Query("""
            update Product p set p.stock = p.stock - :qty, p.version = p.version + 1
            where p.id = :id and p.active = true and p.stock >= :qty""")
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("update Product p set p.stock = p.stock + :qty, p.version = p.version + 1 where p.id = :id")
    int incrementStock(@Param("id") Long id, @Param("qty") int qty);
}
