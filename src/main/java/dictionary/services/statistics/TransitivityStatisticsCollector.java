package dictionary.services.statistics;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TransitivityStatisticsCollector {

    @Autowired
    private WordRepo dictionary;

    public float getProbabilityThatTransitivityWorks() {
        int amountOfPositiveOutcomes = 0;
        int totalAmountOfOutcomes = 0;

        int dictionarySize = dictionary.getWordAmount();
        for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
            Word currentWord = dictionary.getWordByIndex(currentIndex);
            Map<Word, Float> firstLevelSynonymsMap = dictionary.getSynonymsForWord(currentWord);
            Set<Word> firstLevelSynonyms = firstLevelSynonymsMap.keySet();
            Set<Word> secondLevelSynonyms = new HashSet<>();
            for (Word firstLevelSynonym : firstLevelSynonyms) {
                Map<Word, Float> secondLevelSynonymsMap = dictionary.getSynonymsForWord(firstLevelSynonym);
                secondLevelSynonyms.addAll(secondLevelSynonymsMap.keySet());
            }
            for (Word word : secondLevelSynonyms) {
                if (firstLevelSynonyms.contains(word)) {
                    ++amountOfPositiveOutcomes;
                }
            }
            totalAmountOfOutcomes += secondLevelSynonyms.size();

            // Test
            if (currentIndex % 1000 == 0) {
                System.out.println("Amount of checked words: " + currentIndex);
            }
        }

        return ((float) amountOfPositiveOutcomes / (float) totalAmountOfOutcomes);
    }
}
