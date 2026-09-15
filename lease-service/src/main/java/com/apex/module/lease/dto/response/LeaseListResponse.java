package com.apex.module.lease.dto.response;

import java.util.List;

public class LeaseListResponse {
    private List<LeaseResponse> leases;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public LeaseListResponse() {}

    public LeaseListResponse(List<LeaseResponse> leases, int page, int size,
                             long totalElements, int totalPages) {
        this.leases = leases;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<LeaseResponse> getLeases() { return leases; }
    public void setLeases(List<LeaseResponse> leases) { this.leases = leases; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
