package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class GrammarList {
    private List<GrammarData> grammars;
    private PaginationResponse pagination;
}
