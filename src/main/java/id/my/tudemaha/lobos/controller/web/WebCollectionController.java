package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.exception.QuotaExceededException;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/collections")
public class WebCollectionController {
    private final CollectionService collectionService;

    public WebCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public String index(
            @AuthenticationPrincipal User user, @ModelAttribute PaginationRequest paginationRequest, Model model) {
        CollectionList collectionList = collectionService.getCollectionsByUserId(user.getId(), paginationRequest);
        int activeCount = collectionService.getActiveCollectionCount(user.getId());

        model.addAttribute("collectionList", collectionList);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("maxQuota", CollectionService.MAX_COLLECTION_QUOTA);
        model.addAttribute("quotaReached", activeCount >= CollectionService.MAX_COLLECTION_QUOTA);
        model.addAttribute("hasNotifiedDeveloper", collectionService.hasUserNotifiedDeveloper(user.getId()));

        return "collections/index";
    }

    @PostMapping
    public String createCollection(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("createCollection") CreateCollection createCollection,
            RedirectAttributes redirectAttributes) {
        try {
            collectionService.createCollection(createCollection, user.getId());
        } catch (QuotaExceededException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/collections";
    }

    @PostMapping("/quota-interaction")
    @ResponseBody
    public ResponseEntity<Void> recordQuotaInteraction(
            @AuthenticationPrincipal User user, @RequestParam("action") String action) {
        collectionService.recordQuotaInteraction(user, action);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public String updateCollection(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @ModelAttribute("createCollection") CreateCollection createCollection) {
        collectionService.update(createCollection, id, user.getId());
        return "redirect:/collections";
    }

    @DeleteMapping("/{id}")
    public String deleteCollection(@AuthenticationPrincipal User user, @PathVariable String id) {
        collectionService.delete(id, user.getId());
        return "redirect:/collections";
    }
}
