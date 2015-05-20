package dictionary.repositories;

import dictionary.entities.Word;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring_contexts/test_context.xml")
public class WordRepoTest {

    @Autowired
    private WordRepo wordRepo;

    private Word firstWord;

    private Word secondWord;

    private Word thirdWord;

    @Before
    public void setUp() throws Exception {
        firstWord = new Word();
        firstWord.setValue("First word");

        secondWord = new Word();
        secondWord.setValue("Second word");

        thirdWord = new Word();
        thirdWord.setValue("Third word");

        wordRepo.saveWord(firstWord);
        wordRepo.saveWord(secondWord);
        wordRepo.saveWord(thirdWord);

        wordRepo.addSynonymForWord(firstWord, secondWord, 1.0f);
    }

    @Test
    public void getExistingWordByIndex() throws Exception {
        assertEquals(firstWord, wordRepo.getWordByIndex(0));
        assertEquals(secondWord, wordRepo.getWordByIndex(1));
    }

    @Test
    public void getNonExistingWordByIndex() throws Exception {
        assertEquals(null, wordRepo.getWordByIndex(4));
    }

    @Test
    public void getExistingWordByValue() throws Exception {
        assertEquals(firstWord, wordRepo.getWordByValue(firstWord.getValue()));
        assertEquals(secondWord, wordRepo.getWordByValue(secondWord.getValue()));
    }

    @Test
    public void getNonExistingWordByValue() throws Exception {
        assertEquals(null, wordRepo.getWordByValue("NonExistingWord"));
        assertEquals(null, wordRepo.getWordByValue(null));
    }

    @Test
    public void getWordAmount() throws Exception {
        assertEquals(3, wordRepo.getWordAmount());
    }
    
    @Test
    public void getSynonymsForWord() throws Exception {
        Map<Word, Float> synonyms = wordRepo.getSynonymsForWord(firstWord);
        assertEquals(1, synonyms.size());
        assertTrue(synonyms.containsKey(secondWord));
        assertEquals(1.0f, synonyms.get(secondWord), 0.001f);
    }

    @Test
    public void deleteAllUnboundWords() throws Exception {
        assertEquals(3, wordRepo.getWordAmount());
        wordRepo.deleteAllUnboundWords();
        assertEquals(2, wordRepo.getWordAmount());
        assertEquals(null, wordRepo.getWordByValue(thirdWord.getValue()));
    }

    @Test
    public void changeProbabilityForSynonym() throws Exception {
        wordRepo.changeProbabilityForSynonym(firstWord, secondWord, 0.5f);
        Map<Word, Float> synonyms = wordRepo.getSynonymsForWord(firstWord);
        assertEquals(0.5f, synonyms.get(secondWord), 0.001f);
    }
}