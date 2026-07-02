package id.my.tudemaha.lobos.dto.request;

import lombok.Data;

@Data
public class PaginationRequest {
    private Integer page;
    private Integer perPage;
    private String query;
}

