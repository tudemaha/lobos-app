package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.response.CollectionData;
import id.my.tudemaha.lobos.model.Collection;

public class CollectionMapper {
    public static Collection toEntity(CreateCollection request) {
        Collection collection = new Collection();
        collection.setName(request.getName());
        collection.setColor(request.getColor());

        return collection;
    }

    public static CollectionData toDto(Collection collection) {
        CollectionData collectionData = new CollectionData();
        collectionData.setId(collection.getId());
        collectionData.setName(collection.getName());
        collectionData.setColor(collection.getColor());
        collectionData.setCreatedAt(collection.getCreatedAt());

        return collectionData;
    }
}
