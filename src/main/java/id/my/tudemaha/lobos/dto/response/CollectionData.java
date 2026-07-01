package id.my.tudemaha.lobos.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectionData {
    private String id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
}
