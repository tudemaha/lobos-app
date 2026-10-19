package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.response.SyncChangesData;
import id.my.tudemaha.lobos.dto.response.SyncCollectionData;
import id.my.tudemaha.lobos.dto.response.SyncGrammarData;
import id.my.tudemaha.lobos.model.Collection;
import id.my.tudemaha.lobos.model.Grammar;
import id.my.tudemaha.lobos.repository.CollectionRepository;
import id.my.tudemaha.lobos.repository.GrammarRepository;
import id.my.tudemaha.lobos.utils.SyncCursor;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SyncService {
    @Value("${app.sync.tombstone-retention}")
    private Duration syncTombstoneRetention;

    private final CollectionRepository collectionRepository;
    private final GrammarRepository grammarRepository;

    public SyncService(CollectionRepository collectionRepository, GrammarRepository grammarRepository) {
        this.collectionRepository = collectionRepository;
        this.grammarRepository = grammarRepository;
    }

    public SyncChangesData getChanges(String userId, String rawCursor, int limit) {
        int safeLimit = Math.clamp(limit, 1, 500);

        SyncCursor cursor = SyncCursor.decode(rawCursor);
        LocalDateTime since = cursor != null ? cursor.updatedAt() : null;
        String lastId = cursor != null ? cursor.id() : null;

        int fetchSize = safeLimit + 1;
        List<Collection> rawCollections = collectionRepository.findSyncChanges(userId, since, lastId, fetchSize);
        List<Grammar> rawGrammars = grammarRepository.findSyncChanges(userId, since, lastId, fetchSize);

        record TimelineItem(LocalDateTime updatedAt, String id, Collection collection, Grammar grammar) {}

        List<TimelineItem> timeline = new ArrayList<>();
        for (Collection c : rawCollections) {
            timeline.add(new TimelineItem(c.getUpdatedAt(), c.getId(), c, null));
        }
        for (Grammar g : rawGrammars) {
            timeline.add(new TimelineItem(g.getUpdatedAt(), g.getId(), null, g));
        }

        timeline.sort(Comparator.comparing(TimelineItem::updatedAt).thenComparing(TimelineItem::id));

        boolean hasMore = timeline.size() > safeLimit;
        List<TimelineItem> pageItems = hasMore ? timeline.subList(0, safeLimit) : timeline;

        List<SyncCollectionData> collections = new ArrayList<>();
        List<SyncGrammarData> grammars = new ArrayList<>();

        for (TimelineItem item : pageItems) {
            if (item.collection != null) {
                Collection c = item.collection;
                collections.add(new SyncCollectionData(
                        c.getId(), c.getName(), c.getColor(), c.getCreatedAt(), c.getUpdatedAt(), c.getDeletedAt()));
            } else if (item.grammar != null) {
                Grammar g = item.grammar;
                grammars.add(new SyncGrammarData(
                        g.getId(),
                        g.getCollectionId(),
                        g.getWord(),
                        g.getMeaning(),
                        g.getExample(),
                        g.isStarred(),
                        g.getCreatedAt(),
                        g.getUpdatedAt(),
                        g.getDeletedAt()));
            }
        }

        String nextCursor = rawCursor;
        if (!pageItems.isEmpty()) {
            TimelineItem lastItem = pageItems.getLast();
            nextCursor = new SyncCursor(lastItem.updatedAt(), lastItem.id()).encode();
        }

        return new SyncChangesData(collections, grammars, nextCursor, hasMore, LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldTombstones() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(syncTombstoneRetention.toDays());
        int grammarsPurged = grammarRepository.purgeOldTombstones(threshold);
        int collectionsPurged = collectionRepository.purgeOldTombstones(threshold);
        log.info("Purged old tombstones: {} collections, {} grammars", collectionsPurged, grammarsPurged);
    }
}
