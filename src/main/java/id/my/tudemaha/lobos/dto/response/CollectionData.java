package id.my.tudemaha.lobos.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CollectionData {
    private String id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
