package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.CreateGrammar;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.request.UpdateGrammar;
import id.my.tudemaha.lobos.dto.response.GrammarDetail;
import id.my.tudemaha.lobos.dto.response.GrammarList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.GrammarService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/collections/{collectionId}/grammars")
public class WebGrammarController {
    private final GrammarService grammarService;

    public WebGrammarController(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    @GetMapping
    public String getGrammars(
            @AuthenticationPrincipal User user,
            @ModelAttribute PaginationRequest paginationRequest,
            @PathVariable String collectionId,
            Model model
    ) {
        GrammarList grammarList =
                grammarService.getGrammarsByCollectionId(
                        collectionId,
                        user.getId(),
                        paginationRequest
                );
        model.addAttribute("grammarList", grammarList);

        return "grammars/index";
    }

    @GetMapping("/{grammarId}")
    public String getGrammar(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @AuthenticationPrincipal User user,
            Model model
    ) {
        GrammarDetail grammarDetail =
                grammarService.getGrammarById(
                        grammarId,
                        collectionId,
                        user.getId()
                );

        model.addAttribute("grammar", grammarDetail);
        return "grammars/detail";
    }

    @PostMapping
    public String createGrammars(
            @Valid @ModelAttribute("createGrammar") CreateGrammar createGrammar,
            @AuthenticationPrincipal User user,
            @PathVariable String collectionId
    ) {
        grammarService.createGrammar(createGrammar, collectionId, user.getId());

        return "redirect:/collections/" + collectionId + "/grammars";
    }

    @PutMapping("/{grammarId}")
    public String updateGrammars(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @Valid @ModelAttribute UpdateGrammar updateGrammar,
            @AuthenticationPrincipal User user
    ) {
        grammarService.update(
                updateGrammar,
                grammarId,
                collectionId,
                user.getId()
        );

        return "redirect:/collections/" + collectionId + "/grammars/" + grammarId;
    }

    @PatchMapping("/{grammarId}")
    public String starGrammars(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @RequestParam(required = false) String redirectTo,
            @AuthenticationPrincipal User user
    ) {
        grammarService.toggleStarred(grammarId, collectionId, user.getId());

        if ("list".equals(redirectTo)) {
            return "redirect:/collections/" + collectionId + "/grammars";
        }
        return "redirect:/collections/" + collectionId + "/grammars/" + grammarId;
    }

    @DeleteMapping("{grammarId}")
    public String deleteGrammars(
            @PathVariable String collectionId,
            @PathVariable String grammarId,
            @AuthenticationPrincipal User user
    ) {
        grammarService.delete(grammarId, collectionId, user.getId());

        return "redirect:/collections/" + collectionId + "/grammars";
    }
}
