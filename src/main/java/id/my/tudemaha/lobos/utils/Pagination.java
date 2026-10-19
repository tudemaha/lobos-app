package id.my.tudemaha.lobos.utils;

import id.my.tudemaha.lobos.dto.request.PaginationRequest;

public class Pagination {
    public static PaginationRequest buildPaginationRequest(PaginationRequest paginationRequest) {
        int page = (paginationRequest.getPage() == null || paginationRequest.getPage() < 1)
                ? 1
                : paginationRequest.getPage();
        int perPage = paginationRequest.getPerPage() == null ? 10 : Math.clamp(paginationRequest.getPerPage(), 10, 100);

        paginationRequest.setPage(page);
        paginationRequest.setPerPage(perPage);
        return paginationRequest;
    }
}
