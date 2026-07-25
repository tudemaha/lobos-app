package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebCollectionController {
    private final CollectionService collectionService;

    public WebCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/")
    public String index(
            @AuthenticationPrincipal User user,
            @ModelAttribute PaginationRequest paginationRequest,
            Model model
    ) {
        CollectionList collectionList =
                collectionService.getCollectionsByUserId(
                        user.getId(),
                        paginationRequest
                );
        model.addAttribute("collectionList", collectionList);

        return "collections/index";
    }

    @PostMapping("/collections")
    public String createCollection(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("createCollection") CreateCollection createCollection
    ) {
        collectionService.createCollection(createCollection, user.getId());
        return "redirect:/";
    }

    @PutMapping("/collections/{id}")
    public String updateCollection(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @ModelAttribute("createCollection") CreateCollection createCollection
    ) {
        collectionService.update(createCollection, id, user.getId());
        return "redirect:/";
    }

    @DeleteMapping("/collections/{id}")
    public String deleteCollection(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        collectionService.delete(id, user.getId());
        return "redirect:/";
    }
}
