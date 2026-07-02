package id.my.tudemaha.lobos.controller;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/collections")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
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
    public ResponseEntity<HttpResponse<Void>> deleteCollection(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        collectionService.delete(id, user.getId());
        return ResponseEntity
                .ok(HttpResponse.success("collection deleted successfully", null));
    }
}
