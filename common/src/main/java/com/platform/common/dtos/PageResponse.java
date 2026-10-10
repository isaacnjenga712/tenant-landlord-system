package com.platform.common.dtos;

import java.util.List;

/**
 * Framework-agnostic paginated response.
 * Never serialize a Spring Page directly — its JSON shape is unstable across versions.
 */
public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        int size,
        boolean hasMore
) {

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), 0L, page, size, false);
    }

    /**
     * Build from a Spring Data Page. Call this from services that already
     * depend on spring-data-commons — it's not defined here to keep `common`
     * free of framework deps.
     */
    public static <T> PageResponse<T> from(
            List<T> items, long total, int page, int size, boolean hasMore) {
        return new PageResponse<>(items, total, page, size, hasMore);
    }
}
