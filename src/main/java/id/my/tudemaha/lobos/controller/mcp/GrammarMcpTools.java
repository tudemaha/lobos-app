package id.my.tudemaha.lobos.controller.mcp;

import id.my.tudemaha.lobos.dto.request.CreateGrammar;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.request.UpdateGrammar;
import id.my.tudemaha.lobos.dto.response.GrammarDetail;
import id.my.tudemaha.lobos.dto.response.GrammarList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.GrammarService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class GrammarMcpTools {
    private final GrammarService grammarService;

    public GrammarMcpTools(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    private User currentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    @McpTool(name = "list_grammars", description = "List authenticated user's grammars from a collection, paginated and optionally filtered by name")
    public GrammarList listGrammars(
            @McpToolParam(description = "1-based page number") Integer page,
            @McpToolParam(description = "items per page") Integer perPage,
            @McpToolParam(required = false, description = "optional case-insensitive substring filter on the grammar's word or meaning") String query,
            @McpToolParam(description = "id of the collection to get its grammars") String collectionId
    ) {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPage(page);
        paginationRequest.setPerPage(perPage);
        paginationRequest.setQuery(query);

        return grammarService.getGrammarsByCollectionId(collectionId, currentUser().getId(), paginationRequest);
    }

    @McpTool(name = "get_grammar", description = "Get a grammar's detail from a collection")
    public GrammarDetail getGrammar(
            @McpToolParam(description = "id of the grammar to get its detail") String grammarId,
            @McpToolParam(description = "id of the collection which contains the grammar") String collectionId
    ) {
        return grammarService.getGrammarById(grammarId, collectionId, currentUser().getId());
    }

    @McpTool(name = "create_grammar", description = "Create a new grammar inside a collection for the authenticated user")
    public String createGrammar(
            @McpToolParam(description = "the word, 1-100 characters") String word,
            @McpToolParam(description = "the meaning of the word") String meaning,
            @McpToolParam(required = false, description = "example of a grammar") String example,
            @McpToolParam(description = "id of the collection to store the grammar") String collectionId
    ) {
        CreateGrammar createGrammar = new CreateGrammar();
        createGrammar.setWord(word);
        createGrammar.setMeaning(meaning);
        createGrammar.setExample(example);
        grammarService.createGrammar(createGrammar, collectionId, currentUser().getId());

        return "grammar created successfully";
    }

    @McpTool(name = "update_grammar", description = "Update an existing grammar's word, meaning, and example")
    public String updateGrammar(
            @McpToolParam(description = "the word, 1-100 characters") String word,
            @McpToolParam(description = "the meaning of the word") String meaning,
            @McpToolParam(required = false, description = "example of a grammar") String example,
            @McpToolParam(description = "id of the collection to store the grammar") String collectionId,
            @McpToolParam(description = "id of the grammar to update") String grammarId
    ) {
        UpdateGrammar updateGrammar = new UpdateGrammar();
        updateGrammar.setWord(word);
        updateGrammar.setMeaning(meaning);
        updateGrammar.setExample(example);
        grammarService.update(updateGrammar, grammarId, collectionId, currentUser().getId());

        return "grammar updated successfully";
    }

    @McpTool(name = "toggle_star_grammar", description = "Toggle the star/favorite of a grammar")
    public String toggleStarGrammar(
            @McpToolParam(description = "id of the grammar to toggle the star") String grammarId,
            @McpToolParam(description = "id of the collection where the grammar stored") String collectionId
    ) {
        grammarService.toggleStarred(grammarId, collectionId, currentUser().getId());

        return "grammar star toggled successfully";
    }

    @McpTool(name = "delete_grammar", description = "Delete a grammar inside a collection, owned by the authenticated user")
    public String deleteGrammar(
            @McpToolParam(description = "id of the grammar to delete") String grammarId,
            @McpToolParam(description = "id of the collection where the grammar stored") String collectionId
    ) {
        grammarService.delete(grammarId, collectionId, currentUser().getId());

        return "grammar deleted successfully";
    }
}
