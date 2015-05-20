package dictionary.services.mergers;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import dictionary.util.DictionaryRepoQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictionaryMerger {

    @Autowired
    private WordRepo mergedDictionary;

    @Autowired
    @DictionaryRepoQualifier
    private List<WordRepo> dictionaries;

    @Transactional
    public void mergeDictionaries() {
        copyWordsToMergedDictionary();

        // Test
        System.out.println("All words were loaded, time to load synonyms");

        int dictionarySize = mergedDictionary.getWordAmount();
        for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
            Word currentWord = mergedDictionary.getWordByIndex(currentIndex);
            Map<String, Integer> synonymOccurrences = getSynonymOccurrencesForWord(currentWord);
            addSynonymsToMergedDictionary(currentWord, synonymOccurrences);

            // Test
            if (currentIndex % 1000 == 0) {
                System.out.println("Amount of checked words: " + currentIndex);
            }
        }
    }

    private void copyWordsToMergedDictionary() {
        for (WordRepo dictionary : dictionaries) {
            int dictionarySize = dictionary.getWordAmount();
            for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
                Word word = dictionary.getWordByIndex(currentIndex);
                if (mergedDictionary.getWordByValue(word.getValue()) == null) {
                    Word newWord = new Word();
                    newWord.setValue(word.getValue());
                    mergedDictionary.saveWord(newWord);
                }
            }
        }
    }

    private Map<String, Integer> getSynonymOccurrencesForWord(Word wordOfInterest) {
        Map<String, Integer> synonymOccurrences = new HashMap<>();
        for (WordRepo dictionary : dictionaries) {
            Word word = dictionary.getWordByValue(wordOfInterest.getValue());
            if (word != null) {
                Map<Word, Float> synonyms = dictionary.getSynonymsForWord(word);
                for (Word synonym : synonyms.keySet()) {
                    String value = synonym.getValue();
                    if (synonymOccurrences.containsKey(value)) {
                        int amountOfOccurrences = synonymOccurrences.get(value);
                        synonymOccurrences.put(value, amountOfOccurrences + 1);
                    } else {
                        synonymOccurrences.put(value, 1);
                    }
                }
            }
        }
        return synonymOccurrences;
    }

    private void addSynonymsToMergedDictionary(Word wordFromMergedDictionary, Map<String, Integer> synonymOccurrences) {
        for (Map.Entry<String, Integer> synonymOccurrence : synonymOccurrences.entrySet()) {
            Word synonym = mergedDictionary.getWordByValue(synonymOccurrence.getKey());
            float probability = ((float) synonymOccurrence.getValue()) / dictionaries.size();
            mergedDictionary.addSynonymForWord(wordFromMergedDictionary, synonym, probability);
        }
    }
}
