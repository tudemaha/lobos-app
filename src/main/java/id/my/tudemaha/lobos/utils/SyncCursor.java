package id.my.tudemaha.lobos.utils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public record SyncCursor(LocalDateTime updatedAt, String id) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static SyncCursor decode(String rawCursor) {
        if (rawCursor == null || rawCursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(rawCursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split("_", 2);
            if (parts.length != 2) {
                return null;
            }
            return new SyncCursor(LocalDateTime.parse(parts[0], FORMATTER), parts[1]);
        } catch (Exception e) {
            return null;
        }
    }

    public String encode() {
        String raw = updatedAt.toString() + "_" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
