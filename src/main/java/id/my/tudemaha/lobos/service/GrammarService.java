package id.my.tudemaha.lobos.service;

import id.my.tudemaha.lobos.dto.request.CreateGrammar;
import id.my.tudemaha.lobos.dto.request.PaginationRequest;
import id.my.tudemaha.lobos.dto.request.UpdateGrammar;
import id.my.tudemaha.lobos.dto.response.GrammarData;
import id.my.tudemaha.lobos.dto.response.GrammarDetail;
import id.my.tudemaha.lobos.dto.response.GrammarList;
import id.my.tudemaha.lobos.dto.response.PaginationResponse;
import id.my.tudemaha.lobos.exception.ForbiddenAccessException;
import id.my.tudemaha.lobos.exception.NotFoundException;
import id.my.tudemaha.lobos.mapper.GrammarMapper;
import id.my.tudemaha.lobos.model.Collection;
import id.my.tudemaha.lobos.model.Grammar;
import id.my.tudemaha.lobos.repository.CollectionRepository;
import id.my.tudemaha.lobos.repository.GrammarRepository;
import id.my.tudemaha.lobos.utils.Pagination;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GrammarService {
    private final GrammarRepository grammarRepository;
    private final CollectionRepository collectionRepository;

    public GrammarService(
            GrammarRepository grammarRepository,
            CollectionRepository collectionRepository
    ) {
        this.grammarRepository = grammarRepository;
        this.collectionRepository = collectionRepository;
    }

    private void checkCollectionAccess(String collectionId, String userId) {
        Optional<Collection> collectionOpt = collectionRepository.findById(collectionId);
        if (collectionOpt.isEmpty()) {
            throw new NotFoundException("collection with id " + collectionId + " not found");
        }

        Collection collection = collectionOpt.get();
        if (!collection.getUserId().equals(userId)) {
            throw new ForbiddenAccessException("forbidden access to grammar resource");
        }
    }

    private Grammar checkGrammarAccess(String grammarId, String collectioinId, String userId) {
        Optional<Grammar> grammarOpt = grammarRepository.findById(grammarId);
        if (grammarOpt.isEmpty()) {
            throw new NotFoundException("grammar with id " + grammarId + " not found");
        }
        Grammar grammar = grammarOpt.get();
        if (!grammar.getCollectionId().equals(collectioinId)) {
            throw new ForbiddenAccessException("forbidden access to grammar resource");
        }

        checkCollectionAccess(grammar.getCollectionId(), userId);
        return grammar;
    }

    public void createGrammar(CreateGrammar createGrammar, String collectionId, String userId) {
        checkCollectionAccess(collectionId, userId);
        Grammar grammar = GrammarMapper.toEntity(createGrammar);
        grammar.setCollectionId(collectionId);
        grammarRepository.insert(grammar);
    }

    public GrammarList getGrammarsByCollectionId(
            String collectionId,
            String userId,
            PaginationRequest paginationRequest
    ) {
        checkCollectionAccess(collectionId, userId);

        PaginationRequest validPaginationRequest = Pagination.buildPaginationRequest(paginationRequest);
        GrammarRepository.PaginatedGrammar paginatedGrammar = grammarRepository
                .findAllByCollectionId(collectionId, validPaginationRequest);

        List<GrammarData> grammarData = paginatedGrammar.grammars()
                .stream()
                .map(GrammarMapper::toDto)
                .toList();

        PaginationResponse paginationResponse = new PaginationResponse();
        paginationResponse.setPage(validPaginationRequest.getPage());
        paginationResponse.setTotalPage(
                Math.ceilDiv(paginatedGrammar.totalCount(), paginationRequest.getPerPage())
        );

        GrammarList grammarList = new GrammarList();
        grammarList.setGrammars(grammarData);
        grammarList.setPagination(paginationResponse);
        return grammarList;
    }

    public GrammarDetail getGrammarById(String grammarID, String collectionId, String userId) {
        Grammar grammar = checkGrammarAccess(grammarID, collectionId, userId);
        return GrammarMapper.toDetailDto(grammar);
    }

    public void update(UpdateGrammar updateGrammar, String grammarId, String collectionId, String userId) {
        Grammar grammar = checkGrammarAccess(grammarId, collectionId, userId);
        grammar.setWord(updateGrammar.getWord());
        grammar.setMeaning(updateGrammar.getMeaning());
        grammar.setExample(updateGrammar.getExample());
        grammarRepository.update(grammar);
    }

    public void toggleStarred(String grammarId, String collectionId, String userId) {
        Grammar grammar = checkGrammarAccess(grammarId, collectionId, userId);
        grammarRepository.toggleStarred(grammar.getId());
    }

    public void delete(String grammarId, String collectionId, String userId) {
        Grammar grammar = checkGrammarAccess(grammarId, collectionId, userId);
        grammarRepository.delete(grammar.getId());
    }
}
