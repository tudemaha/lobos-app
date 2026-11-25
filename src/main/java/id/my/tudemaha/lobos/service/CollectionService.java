package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateCollection;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.response.CollectionData;
import id.my.tudemaha.lobos.dto.response.CollectionList;
import id.my.tudemaha.lobos.dto.response.PaginationResponse;
import id.my.tudemaha.lobos.exception.DuplicateResourceException;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.NotFoundException;
import id.my.tudemaha.lobos.exception.QuotaExceededException;
import id.my.tudemaha.lobos.mapper.CollectionMapper;
import id.my.tudemaha.lobos.model.Collection;
import id.my.tudemaha.lobos.model.User;
import id.my.tudemaha.lobos.repository.CollectionRepository;
import id.my.tudemaha.lobos.repository.GrammarRepository;
import id.my.tudemaha.lobos.repository.QuotaInteractionRepository;
import id.my.tudemaha.lobos.utils.Pagination;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionService {
    public static final int MAX_COLLECTION_QUOTA = 3;
    public static final String ACTION_CLICK_ADD = "CLICK_ADD_COLLECTION";
    public static final String ACTION_NOTIFY_DEV = "NOTIFY_DEVELOPER";

    private final CollectionRepository collectionRepository;
    private final GrammarRepository grammarRepository;
    private final QuotaInteractionRepository quotaInteractionRepository;
    private final EmailService emailService;

    public CollectionService(
            CollectionRepository collectionRepository,
            GrammarRepository grammarRepository,
            QuotaInteractionRepository quotaInteractionRepository,
            EmailService emailService) {
        this.collectionRepository = collectionRepository;
        this.grammarRepository = grammarRepository;
        this.quotaInteractionRepository = quotaInteractionRepository;
        this.emailService = emailService;
    }

    public CollectionData createCollection(CreateCollection createCollection, String userId) {
        if (createCollection.getId() != null && !createCollection.getId().isBlank()) {
            Optional<Collection> collectionOpt = collectionRepository.findById(createCollection.getId());
            if (collectionOpt.isPresent()) {
                Collection collection = collectionOpt.get();
                if (!collection.getUserId().equals(userId)) {
                    throw new DuplicateResourceException("Resource with this ID already exists");
                }
                return CollectionMapper.toDto(collection);
            }
        } else {
            createCollection.setId(UUID.randomUUID().toString());
        }

        if (collectionRepository.countActiveByUserId(userId) >= MAX_COLLECTION_QUOTA) {
            throw new QuotaExceededException(
                    "Collection quota exceeded. Maximum allowed is " + MAX_COLLECTION_QUOTA + " collections.");
        }

        Collection collection = CollectionMapper.toEntity(createCollection);
        collection.setUserId(userId);
        collectionRepository.insert(collection);

        return CollectionMapper.toDto(collection);
    }

    public int getActiveCollectionCount(String userId) {
        return collectionRepository.countActiveByUserId(userId);
    }

    public boolean hasUserNotifiedDeveloper(String userId) {
        return quotaInteractionRepository.hasUserNotifiedDeveloper(userId);
    }

    public void recordQuotaInteraction(User user, String action) {
        if (!ACTION_CLICK_ADD.equals(action) && !ACTION_NOTIFY_DEV.equals(action)) {
            throw new IllegalArgumentException("Invalid quota interaction action");
        }

        if (ACTION_NOTIFY_DEV.equals(action)) {
            boolean alreadyNotified = quotaInteractionRepository.hasUserNotifiedDeveloper(user.getId());
            quotaInteractionRepository.insert(UUID.randomUUID().toString(), user.getId(), action);
            if (!alreadyNotified) {
                emailService.sendQuotaInterestEmail(user.getEmail(), user.getFirstName());
            }
        } else {
            quotaInteractionRepository.insert(UUID.randomUUID().toString(), user.getId(), action);
        }
    }

    public CollectionList getCollectionsByUserId(String id, PaginationRequest paginationRequest) {
        PaginationRequest validPaginationRequest = Pagination.buildPaginationRequest(paginationRequest);
        CollectionRepository.PaginatedCollection paginatedCollection =
                collectionRepository.findAllByUserId(id, validPaginationRequest);

        List<CollectionData> collectionData = paginatedCollection.collections().stream()
                .map(CollectionMapper::toDto)
                .toList();

        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setPage(validPaginationRequest.getPage());
        paginationResponse.setTotalPage(
                Math.ceilDiv(paginatedCollection.totalCount(), validPaginationRequest.getPerPage()));

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

    @Transactional
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
        grammarRepository.deleteAllByCollectionId(collectionId);
    }
}
