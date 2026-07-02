package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.response.CollectionData;
import id.my.tudemaha.lobos.dto.response.CollectionList;
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

    public CollectionList getCollectionsByUserId(String id) {
        List<Collection> collections = collectionRepository.findAllByUserId(id);

        List<CollectionData> collectionDataList = collections
                .stream()
                .map(CollectionMapper::toDto)
                .toList();

        CollectionList collectionList = new CollectionList();
        collectionList.setCollections(collectionDataList);
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
