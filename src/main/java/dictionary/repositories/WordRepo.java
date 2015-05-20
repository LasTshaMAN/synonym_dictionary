package dictionary.repositories;

import dictionary.entities.Word;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface WordRepo extends Repository<Word, Long> {

    public Word getWordByIndex(int index);

    public Word getWordByValue(String value);

    public int getWordAmount();

    public Map<Word, Float> getSynonymsForWord(Word word);

    public void saveWord(Word newWord);

    public void addSynonymForWord(Word word, Word synonym, Float probability);

    public void changeProbabilityForSynonym(Word word, Word synonym, Float newProbability);

    public void deleteAllUnboundWords();

    /**
     *  It is necessary to call "flushSynonyms" for data to be synchronized with database
     */
    public void addSynonymForWordLazily(Word word, Word synonym, Float probability);

    public void flushSynonyms();
}
