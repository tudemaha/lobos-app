package id.my.tudemaha.lobos.utils;

import id.my.tudemaha.lobos.dto.request.PaginationRequest;

public class Pagination {
    public static PaginationRequest buildPaginationRequest(PaginationRequest paginationRequest) {
        if(paginationRequest.getPage() == null || paginationRequest.getPage() < 1) paginationRequest.setPage(1);
        if(paginationRequest.getPerPage() == null || paginationRequest.getPerPage() < 10) paginationRequest.setPerPage(10);
        return paginationRequest;
    }

}
