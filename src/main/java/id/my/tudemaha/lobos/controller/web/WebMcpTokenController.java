package id.my.tudemaha.lobos.controller.web;

import id.my.tudemaha.lobos.dto.request.CreateMcpToken;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CreateTokenResponse;
import id.my.tudemaha.lobos.dto.response.McpTokenList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.McpTokenService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tokens")
public class WebMcpTokenController {
    private final McpTokenService mcpTokenService;

    public WebMcpTokenController(McpTokenService mcpTokenService) {
        this.mcpTokenService = mcpTokenService;
    }

    @GetMapping
    public String index(
            @AuthenticationPrincipal User user,
            @ModelAttribute PaginationRequest paginationRequest,
            Model model
    ) {
        McpTokenList mcpTokenList =
                mcpTokenService.getMcpTokensByUserId(user.getId(), paginationRequest);
        model.addAttribute("mcpTokenList", mcpTokenList);

        return "mcp/index";
    }

    @PostMapping
    public String createToken(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("createMcpToken") CreateMcpToken createMcpToken,
            RedirectAttributes redirectAttributes
    ) {
        CreateTokenResponse createTokenResponse =
                mcpTokenService.generateToken(createMcpToken, user.getId());
        redirectAttributes.addFlashAttribute("newToken", createTokenResponse.getToken());

        return "redirect:/tokens";
    }

    @PatchMapping("/{id}")
    public String updateName(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @Valid @ModelAttribute("createMcpToken") CreateMcpToken createMcpToken
    ) {
        mcpTokenService.updateName(createMcpToken, id, user.getId());

        return "redirect:/tokens";
    }

    @DeleteMapping("/{id}")
    public String deleteToken(
            @AuthenticationPrincipal User user,
            @PathVariable String id
    ) {
        mcpTokenService.delete(id, user.getId());

        return "redirect:/tokens";
    }
}
