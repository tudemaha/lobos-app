package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.dto.response.SyncChangesData;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync", description = "Offline-first delta sync endpoints")
public class SyncController {
    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping
    @Operation(summary = "Get delta changes feed since cursor for offline sync")
    public ResponseEntity<HttpResponse<SyncChangesData>> getChanges(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "200") int limit,
            @AuthenticationPrincipal User user
    ) {
        SyncChangesData data = syncService.getChanges(user.getId(),  cursor, limit);
        return ResponseEntity.ok(HttpResponse.success("sync changes fetched successfully", data));
    }
}
