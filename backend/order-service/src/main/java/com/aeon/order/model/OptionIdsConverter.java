package com.aeon.order.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores customization option ids as a sorted, comma-separated string so equal selections
 * compare equal in SQL (used to merge identical cart lines).
 */
@Converter
public class OptionIdsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> ids) {
        return key(ids);
    }

    @Override
    public List<String> convertToEntityAttribute(String db) {
        if (db == null || db.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(db.split(","));
    }

    public static String key(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return String.join(",", ids.stream().map(String::trim).filter(s -> !s.isEmpty()).sorted().distinct().collect(Collectors.toList()));
    }
}
