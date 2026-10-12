package com.aeon.common.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;

    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <E, T> PageResponse<T> of(org.springframework.data.domain.Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).collect(Collectors.toList()),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public List<T> content() { return content; }
    public int page() { return page; }
    public int size() { return size; }
    public long totalElements() { return totalElements; }
    public int totalPages() { return totalPages; }
}
