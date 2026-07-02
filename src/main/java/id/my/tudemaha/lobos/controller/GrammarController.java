package id.my.tudemaha.lobos.controller;

import id.my.tudemaha.lobos.dto.request.CreateGrammar;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.request.UpdateGrammar;
import id.my.tudemaha.lobos.dto.response.GrammarDetail;
import id.my.tudemaha.lobos.dto.response.GrammarList;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.GrammarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/collections/{collectionId}/grammars")
public class GrammarController {
    private final GrammarService grammarService;

    public GrammarController(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    @PostMapping
    public ResponseEntity<HttpResponse<Void>> create(
            @Valid @RequestBody CreateGrammar createGrammar,
            @AuthenticationPrincipal User user,
            @PathVariable String collectionId
    ) {
        grammarService.createGrammar(createGrammar, collectionId, user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(HttpResponse.success("grammar created successfully", null));
    }

    @GetMapping
    public ResponseEntity<HttpResponse<GrammarList>> getAllGrammars(
            @AuthenticationPrincipal User user,
            @PathVariable String collectionId,
            @ModelAttribute PaginationRequest paginationRequest
            ) {
        GrammarList grammarList = grammarService.getGrammarsByCollectionId(
                collectionId,
                user.getId(),
                paginationRequest
        );

        return ResponseEntity.ok(HttpResponse.success("successfully get grammars", grammarList));
    }

    @GetMapping("/{grammarId}")
    public ResponseEntity<HttpResponse<GrammarDetail>> getGrammar(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @AuthenticationPrincipal User user
    ) {
        GrammarDetail grammarDetail = grammarService.getGrammarById(grammarId, collectionId, user.getId());
        return ResponseEntity.ok(HttpResponse.success("successfully get grammar", grammarDetail));
    }

    @PutMapping("{grammarId}")
    public ResponseEntity<HttpResponse<Void>> updateGrammar(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @Valid @RequestBody UpdateGrammar updateGrammar,
            @AuthenticationPrincipal User user
    ) {
        grammarService.update(updateGrammar, grammarId, collectionId, user.getId());
        return ResponseEntity.ok(HttpResponse.success("grammar updated successfully", null));
    }

    @PatchMapping("{grammarId}")
    public ResponseEntity<HttpResponse<Void>> starGrammar(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @AuthenticationPrincipal User user
    ) {
        grammarService.toggleStarred(grammarId, collectionId, user.getId());
        return ResponseEntity.ok(HttpResponse.success("grammar star toggled successfully", null));
    }

    @DeleteMapping("{grammarId}")
    public ResponseEntity<HttpResponse<Void>> deleteGrammar(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @AuthenticationPrincipal User user
    ) {
        grammarService.delete(grammarId, collectionId, user.getId());
        return ResponseEntity.ok(HttpResponse.success("grammar deleted successfully", null));
    }
}
