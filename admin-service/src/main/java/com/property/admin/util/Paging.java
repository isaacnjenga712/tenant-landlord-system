package com.property.admin.util;

import com.platform.common.dtos.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public final class Paging {

    private Paging() {}

    public static <E, T> PageResponse<T> toResponse(Page<E> page, Function<E, T> mapper) {
        return PageResponse.from(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.hasNext()
        );
    }
}
