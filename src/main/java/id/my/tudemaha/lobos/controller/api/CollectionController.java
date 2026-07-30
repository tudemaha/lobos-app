package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "Collections", description = "Collection management endpoints")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    @Operation(summary = "Create a new collection")
    public ResponseEntity<HttpResponse<Void>> createCollection(
            @Valid @RequestBody CreateCollection createCollection,
            @AuthenticationPrincipal User user
            ) {
        collectionService.createCollection(createCollection, user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(HttpResponse.success("collection created successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get all collections")
    public ResponseEntity<HttpResponse<CollectionList>> getAllCollections(
            @AuthenticationPrincipal User user,
            @ModelAttribute PaginationRequest paginationRequest
            ) {
        CollectionList collectionList = collectionService.getCollectionsByUserId(
                user.getId(),
                paginationRequest
        );
        return ResponseEntity
                .ok(HttpResponse.success("successfully get collections", collectionList));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update collection's metadata")
    public ResponseEntity<HttpResponse<Void>> updateCollection(
            @Valid @RequestBody CreateCollection createCollection,
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        collectionService.update(createCollection, id, user.getId());
        return ResponseEntity
                .ok(HttpResponse.success("collection updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a collection")
    public ResponseEntity<HttpResponse<Void>> deleteCollection(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        collectionService.delete(id, user.getId());
        return ResponseEntity
                .ok(HttpResponse.success("collection deleted successfully", null));
    }
}
