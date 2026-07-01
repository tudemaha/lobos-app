package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.mapper.CollectionMapper;
import id.my.tudemaha.lobos.model.Collection;
import id.my.tudemaha.lobos.repository.CollectionRepository;
import org.springframework.stereotype.Service;

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
}
