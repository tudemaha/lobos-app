package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionData;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.dto.response.PaginationResponse;
import id.my.tudemaha.lobos.utils.Pagination;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;

import id.my.tudemaha.lobos.exception.NotFoundException;
import id.my.tudemaha.lobos.mapper.CollectionMapper;
import id.my.tudemaha.lobos.model.Collection;
import id.my.tudemaha.lobos.repository.CollectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    public void createCollection(CreateCollection createCollection, String userId) {
        Collection collection = CollectionMapper.toEntity(createCollection);
        collection.setUserId(userId);

        collectionRepository.insert(collection);
    }

    public CollectionList getCollectionsByUserId(String id, PaginationRequest paginationRequest) {
        PaginationRequest validPaginationRequest = Pagination.buildPaginationRequest(paginationRequest);
        CollectionRepository.PaginatedCollection paginatedCollection = collectionRepository.findAllByUserId(id, validPaginationRequest);

        List<CollectionData> collectionData = paginatedCollection.collections()
                .stream()
                .map(CollectionMapper::toDto)
                .toList();

        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setPage(validPaginationRequest.getPage());
        paginationResponse.setTotalPage(
                Math.ceilDiv(paginatedCollection.totalCount(), validPaginationRequest.getPerPage())
        );

        CollectionList collectionList = new CollectionList();
        collectionList.setCollections(collectionData);
        collectionList.setPagination(paginationResponse);

        return collectionList;
    }


    public void update(CreateCollection createCollection, String collectionId, String userId) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
        if (collectionOpt.isEmpty()) {
            throw new NotFoundException();
        }

        Collection collection = collectionOpt.get();
        if (!collection.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("user is not allowed to update collection");
        }

        collection.setName(createCollection.getName());
        collection.setColor(createCollection.getColor());

        collectionRepository.update(collection);
    }

    public void delete(String collectionId, String userId) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
        if (collectionOpt.isEmpty()) {
            throw new NotFoundException();
        }

        Collection collection = collectionOpt.get();
        if (!collection.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("user is not allowed to delete collection");
        }

        collectionRepository.delete(collectionId);
    }
}
