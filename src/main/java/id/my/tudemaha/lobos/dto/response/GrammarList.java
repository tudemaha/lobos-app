package id.my.tudemaha.lobos.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class GrammarList {
    private List<GrammarData> grammars;
    private PaginationResponse pagination;
}
