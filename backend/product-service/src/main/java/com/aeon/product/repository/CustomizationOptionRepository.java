package com.aeon.product.repository;

import com.aeon.product.model.CustomizationOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomizationOptionRepository extends JpaRepository<CustomizationOption, String> {

    List<CustomizationOption> findAllByOrderByPartAscSortOrderAsc();
}
