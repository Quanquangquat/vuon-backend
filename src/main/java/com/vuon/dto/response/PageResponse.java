package com.vuon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO cho response có phân trang
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> data;
    private long    total;
    private int     page;
    private int     limit;
    private int     totalPages;

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
    }
}
