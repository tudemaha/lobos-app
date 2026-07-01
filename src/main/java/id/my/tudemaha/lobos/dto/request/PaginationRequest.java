package id.my.tudemaha.lobos.dto.request;

import lombok.Data;

@Data
public class PaginationRequest {
    private int page;
    private int perPage;
    private String query;
}
