package id.my.tudemaha.lobos.mapper;

import id.my.tudemaha.lobos.dto.request.CreateGrammar;
import id.my.tudemaha.lobos.dto.response.GrammarData;
import id.my.tudemaha.lobos.dto.response.GrammarDetail;
import id.my.tudemaha.lobos.model.Grammar;

public class GrammarMapper {
    public static Grammar toEntity(CreateGrammar request) {
        Grammar grammar = new Grammar();
        grammar.setWord(request.getWord());
        grammar.setMeaning(request.getMeaning());
        grammar.setExample(request.getExample());

        return grammar;
    }

    public static GrammarData toDto(Grammar entity) {
        GrammarData grammar = new GrammarData();
        grammar.setId(entity.getId());
        grammar.setWord(entity.getWord());
        grammar.setMeaning(entity.getMeaning());
        grammar.setCreatedAt(entity.getCreatedAt());
        grammar.setStarred(entity.isStarred());

        return grammar;
    }

    public static GrammarDetail toDetailDto(Grammar entity) {
        GrammarDetail grammar = new GrammarDetail();
        grammar.setId(entity.getId());
        grammar.setWord(entity.getWord());
        grammar.setMeaning(entity.getMeaning());
        grammar.setExample(entity.getExample());
        grammar.setStarred(entity.isStarred());

        return grammar;
    }
}
