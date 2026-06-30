package id.my.tudemaha.lobos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collection {
    private String id;
    private String name;
    private String color;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
