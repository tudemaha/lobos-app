package id.my.tudemaha.lobos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncChangesData {
    private List<SyncCollectionData> collections;
    private List<SyncGrammarData> grammars;
    private String nextCursor;
    private boolean hasMore;
    private LocalDateTime serverTime;
}
