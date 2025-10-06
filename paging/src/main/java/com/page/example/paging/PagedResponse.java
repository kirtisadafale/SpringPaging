package com.page.example.paging;

import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    // 1-based current page number for clients
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    // 1-based indexes for displayed range
    private long showingFrom;
    private long showingTo;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextPageUrl;
    private String prevPageUrl;

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int currentPage, int pageSize) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public PagedResponse(List<T> content, int currentPage, int pageSize, long totalElements, int totalPages, long showingFrom, long showingTo) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.showingFrom = showingFrom;
        this.showingTo = showingTo;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getShowingFrom() {
        return showingFrom;
    }

    public void setShowingFrom(long showingFrom) {
        this.showingFrom = showingFrom;
    }

    public long getShowingTo() {
        return showingTo;
    }

    public void setShowingTo(long showingTo) {
        this.showingTo = showingTo;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public String getNextPageUrl() {
        return nextPageUrl;
    }

    public void setNextPageUrl(String nextPageUrl) {
        this.nextPageUrl = nextPageUrl;
    }

    public String getPrevPageUrl() {
        return prevPageUrl;
    }

    public void setPrevPageUrl(String prevPageUrl) {
        this.prevPageUrl = prevPageUrl;
    }
}
