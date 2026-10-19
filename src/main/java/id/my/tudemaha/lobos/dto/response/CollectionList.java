package id.my.tudemaha.lobos.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class CollectionList {
    private List<CollectionData> collections;
    private PaginationResponse pagination;
}
