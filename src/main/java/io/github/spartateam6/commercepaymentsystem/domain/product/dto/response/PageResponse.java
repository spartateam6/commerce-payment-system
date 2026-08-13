package io.github.spartateam6.commercepaymentsystem.domain.product.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T> (
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
){
    public static <T> PageResponse<T> od(List<T> content, Page<?> page){
        return new PageResponse<>(
                content,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
