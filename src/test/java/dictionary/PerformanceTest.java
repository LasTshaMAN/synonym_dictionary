package dictionary;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring_contexts/test_context.xml")
public class PerformanceTest {

    @Autowired
    WordRepo wordRepo;

    private static final int MAX_AMOUNT_OF_ITERATIONS = 10000;

    private static final Random RANDOM = new Random();

    private List<Integer> randomIndexes = new ArrayList<>();

    @Test
    public void getExistingWordByIndex() throws Exception {
        long startTime;
        long stopTime;

        List<Word> words = new ArrayList<>();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            Word word = new Word();
            word.setValue(UUID.randomUUID().toString());
            words.add(word);
        }

        startTime = System.currentTimeMillis();
        for (Word word : words) {
            wordRepo.saveWord(word);
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Insert words: " + (stopTime - startTime));

        words.clear();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            Word word = wordRepo.getWordByIndex(i);
            words.add(word);
        }

        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            int randIndex = RANDOM.nextInt(MAX_AMOUNT_OF_ITERATIONS);
            if (randIndex == i) {
                randIndex = (i + 1) % MAX_AMOUNT_OF_ITERATIONS;
            }
            randomIndexes.add(randIndex);
        }

        startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            Word word = words.get(i);
            Word synonym = words.get(randomIndexes.get(i));
            wordRepo.addSynonymForWordLazily(word, synonym, 1.0f);
        }
        wordRepo.flushSynonyms();
        stopTime = System.currentTimeMillis();
        System.out.println("Insert synonyms: " + (stopTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            wordRepo.getWordByValue(words.get(i).getValue());
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Extract words by value: " + (stopTime - startTime));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            wordRepo.getWordByIndex(i);
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Extract words sequentially: " + (stopTime - startTime));

        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            Word word = words.get(i);
            for (int j = i + 1; j < i + 10; ++j) {
                if ((j % MAX_AMOUNT_OF_ITERATIONS) != randomIndexes.get(i)) {
                    Word synonym = words.get(j % MAX_AMOUNT_OF_ITERATIONS);
                    wordRepo.addSynonymForWordLazily(word, synonym, 1.0f);
                }
            }
        }
        wordRepo.flushSynonyms();

        startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_AMOUNT_OF_ITERATIONS; ++i) {
            wordRepo.getSynonymsForWord(words.get(i));
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Extract synonyms: " + (stopTime - startTime));
    }
}
