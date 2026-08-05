package id.my.tudemaha.lobos.controller.api;

import id.my.tudemaha.lobos.dto.request.CreateMcpToken;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CreateTokenResponse;
import id.my.tudemaha.lobos.dto.response.HttpResponse;
import id.my.tudemaha.lobos.dto.response.McpTokenList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.McpTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
@Tag(name = "MCP Token", description = "Manage MCP tokens to be used to connect to MCP server")
public class McpTokenController {
    private final McpTokenService mcpTokenService;

    public McpTokenController(McpTokenService mcpTokenService) {
        this.mcpTokenService = mcpTokenService;
    }

    @PostMapping
    @Operation(summary = "Create an MCP token")
    public ResponseEntity<HttpResponse<CreateTokenResponse>> create(
            @Valid @RequestBody CreateMcpToken createMcpToken,
            @AuthenticationPrincipal User user
    ) {
        CreateTokenResponse createTokenResponse = mcpTokenService.generateToken(
                createMcpToken,
                user.getId()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(HttpResponse.success(
                        "token created successfully",
                        createTokenResponse
                ));
    }

    @GetMapping
    @Operation(summary = "Get all MCP tokens")
    public ResponseEntity<HttpResponse<McpTokenList>> getAllMcpTokens(
            @AuthenticationPrincipal User user,
            @ModelAttribute PaginationRequest paginationRequest
    ) {
        McpTokenList mcpTokenList = mcpTokenService.getMcpTokensByUserId(
                user.getId(),
                paginationRequest
        );

        return ResponseEntity
                .ok(HttpResponse.success("successfully get MCP tokens", mcpTokenList));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update MCP token's name")
    public ResponseEntity<HttpResponse<Void>> updateName(
            @Valid @RequestBody CreateMcpToken createMcpToken,
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        mcpTokenService.updateName(createMcpToken, id, user.getId());
        return ResponseEntity
                .ok(HttpResponse.success("MCP token's name updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete/revoke unused MCP token")
    public ResponseEntity<HttpResponse<Void>> deleteMcpToken(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        mcpTokenService.delete(id, user.getId());
        return ResponseEntity
                .ok(HttpResponse.success("MCP token deleted successfully", null));
    }
}
