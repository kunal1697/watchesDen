package com.aeon.order.repository;

import com.aeon.order.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByIdAsc(Long userId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("delete from CartItem c where c.userId = :userId")
    int deleteAllForUser(@Param("userId") Long userId);
}
