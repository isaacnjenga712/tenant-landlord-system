package com.property.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int size;
    private boolean hasMore;

    public static <E, T> PageResponse<T> of(Page<E> p, Function<E, T> mapper) {
        return PageResponse.<T>builder()
                .items(p.getContent().stream().map(mapper).toList())
                .total(p.getTotalElements())
                .page(p.getNumber())
                .size(p.getSize())
                .hasMore(p.hasNext())
                .build();
    }
}
