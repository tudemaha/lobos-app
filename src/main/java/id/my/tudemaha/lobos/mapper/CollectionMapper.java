package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.model.Collection;

public class CollectionMapper {
    public static Collection toEntity(CreateCollection request) {
        Collection collection = new Collection();
        collection.setName(request.getName());
        collection.setColor(request.getColor());

        return collection;
    }
}
