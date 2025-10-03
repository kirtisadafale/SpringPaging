package com.page.example.paging;

import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private int pageNo;
    private int pageSize;

    public PagedResponse() {
    }

    public PagedResponse(List<T> content, int pageNo, int pageSize) {
        this.content = content;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
