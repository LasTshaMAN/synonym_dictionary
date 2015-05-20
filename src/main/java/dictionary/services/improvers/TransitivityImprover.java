package dictionary.services.improvers;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransitivityImprover {

    @Resource(name="inputDictionary")
    private WordRepo inputDictionary;

    @Autowired
    private WordRepo outputDictionary;

    @Transactional
    public void improveDictionary() {
        copyWordsToOutputDictionary();

        // Test
        System.out.println("All words were loaded, time to load synonyms");

        int dictionarySize = inputDictionary.getWordAmount();
        for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
            Word currentWord = inputDictionary.getWordByIndex(currentIndex);
            Map<Word, Float> synonymsForCurrentWord = inputDictionary.getSynonymsForWord(currentWord);
            Map<Word, Float> synonymsBasedOnTransitivity = getSynonymsBasedOnTransitivity(currentWord, synonymsForCurrentWord);
            enrichSynonymsForWordBySynonymsBasedOnTransitivity(synonymsForCurrentWord, synonymsBasedOnTransitivity);
            addSynonymsToOutputDictionary(currentWord, synonymsForCurrentWord);

            // Test
            if (currentIndex % 1000 == 0) {
                System.out.println("Amount of checked words: " + currentIndex);
            }
        }

        outputDictionary.flushSynonyms();
    }

    private void copyWordsToOutputDictionary() {
        int dictionarySize = inputDictionary.getWordAmount();

        for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
            Word word = inputDictionary.getWordByIndex(currentIndex);
            Word newWord = new Word();
            newWord.setValue(word.getValue());
            outputDictionary.saveWord(newWord);
        }
    }

    private Map<Word, Float> getSynonymsBasedOnTransitivity(Word word, Map<Word, Float> firstLevelSynonyms) {
        Map<Word, Float> synonymsBasedOnTransitivity = new HashMap<>();
        for (Map.Entry<Word, Float> firstLevelSynonym : firstLevelSynonyms.entrySet()) {
            Map<Word, Float> secondLevelSynonyms = inputDictionary.getSynonymsForWord(firstLevelSynonym.getKey());
            for (Map.Entry<Word, Float> secondLevelSynonym : secondLevelSynonyms.entrySet()) {
                Float probability1 = firstLevelSynonym.getValue();
                Float probability2 = secondLevelSynonym.getValue();
                if (synonymsBasedOnTransitivity.containsKey(secondLevelSynonym.getKey())) {
                    Float probability = synonymsBasedOnTransitivity.get(secondLevelSynonym.getKey());
                    synonymsBasedOnTransitivity.put(secondLevelSynonym.getKey(),
                            probability + probability1 * probability2);
                } else {
                    synonymsBasedOnTransitivity.put(secondLevelSynonym.getKey(), probability1 * probability2);
                }
            }
        }
        synonymsBasedOnTransitivity.remove(word);
        normalizeSynonymsBasedOnTransitivity(synonymsBasedOnTransitivity, firstLevelSynonyms.size());
        return synonymsBasedOnTransitivity;
    }

    private void normalizeSynonymsBasedOnTransitivity(Map<Word, Float> synonymsBasedOnTransitivity, int denominator) {
        for (Map.Entry<Word, Float> synonymBasedOnTransitivity : synonymsBasedOnTransitivity.entrySet()) {
            synonymBasedOnTransitivity.setValue(synonymBasedOnTransitivity.getValue() / denominator);
        }
    }

    private void enrichSynonymsForWordBySynonymsBasedOnTransitivity(Map<Word, Float> synonymsForWord,
                                                                    Map<Word, Float> synonymsBasedOnTransitivity) {

        for (Map.Entry<Word, Float> synonymBasedOnTransitivity : synonymsBasedOnTransitivity.entrySet()) {
            Float probabilityBasedOnTransitivity = synonymBasedOnTransitivity.getValue();
            if (synonymsForWord.containsKey(synonymBasedOnTransitivity.getKey())) {
                Float probability = synonymsForWord.get(synonymBasedOnTransitivity.getKey());
                Float improvedProbability = probability + (1.0f - probability) * probabilityBasedOnTransitivity;
                synonymsForWord.put(synonymBasedOnTransitivity.getKey(), improvedProbability);

            } else {
                synonymsForWord.put(synonymBasedOnTransitivity.getKey(), probabilityBasedOnTransitivity);
            }
        }
    }

    private void addSynonymsToOutputDictionary(Word wordFromInputDictionary, Map<Word, Float> synonymsFromInputDictionary) {
        Word word = outputDictionary.getWordByValue(wordFromInputDictionary.getValue());
        for (Map.Entry<Word, Float> synonymFromInputDictionary : synonymsFromInputDictionary.entrySet()) {
            Word synonym = outputDictionary.getWordByValue(synonymFromInputDictionary.getKey().getValue());
            Float probability = synonymFromInputDictionary.getValue();
            outputDictionary.addSynonymForWordLazily(word, synonym, probability);
        }
    }
}
