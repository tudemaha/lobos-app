package id.my.tudemaha.lobos.controller.mcp;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.service.CollectionService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CollectionMcpTools {
    private final CollectionService collectionService;

    public CollectionMcpTools(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    private User currentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }

    @McpTool(name = "list_collections", description = "List the authenticated user's vocabulary collections, paginated and optionally filtered by name")
    public CollectionList listCollections(
            @McpToolParam(description = "1-based page number") Integer page,
            @McpToolParam(description = "items per page") Integer perPage,
            @McpToolParam(required = false, description = "optional case-insensitive substring filter on collection name") String query
    ) {
        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setPage(page);
        paginationRequest.setPerPage(perPage);
        paginationRequest.setQuery(query);

        return collectionService.getCollectionsByUserId(currentUser().getId(), paginationRequest);
    }

    @McpTool(name = "create_collection", description = "Create a new vocabulary collection for the authenticated user")
    public String createCollection(
            @McpToolParam(description = "collection name, 1-100 characters") String name,
            @McpToolParam(description = "hex color, e.g. #FF5733") String color
    ) {
        CreateCollection createCollection = new CreateCollection();
        createCollection.setName(name);
        createCollection.setColor(color);
        collectionService.createCollection(createCollection, currentUser().getId());

        return "collection created successfully";
    }

    @McpTool(name = "update_collection", description = "Update an existing collection's name and color")
    public String updateCollection(
            @McpToolParam(description = "id of the collection to update") String collectionId,
            @McpToolParam(description = "collection name, 1-100 characters") String name,
            @McpToolParam(description = "hex color, e.g. #FF5733") String color
    ) {
        CreateCollection createCollection = new CreateCollection();
        createCollection.setName(name);
        createCollection.setColor(color);
        collectionService.update(createCollection, collectionId, currentUser().getId());

        return "collection updated successfully";
    }

    @McpTool(name = "delete_collection", description = "Delete a collection owned by the authenticated user")
    public String deleteCollection(
            @McpToolParam(description = "id of the collection to delete") String collectionId
    ) {
        collectionService.delete(collectionId, currentUser().getId());

        return "collection deleted successfully";
    }
}
