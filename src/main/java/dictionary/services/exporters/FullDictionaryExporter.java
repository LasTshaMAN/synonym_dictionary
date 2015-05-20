package dictionary.services.exporters;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;

@Service
public class FullDictionaryExporter {

    private final String OUTPUT_FILE_PATH = "result/output.properties";

    @Autowired
    private WordRepo dictionary;

    public void exportFullDictionary() {
        try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE_PATH), "utf-8"))) {
            int dictionarySize = dictionary.getWordAmount();
            for (int currentIndex = 0; currentIndex < dictionarySize; ++currentIndex) {
                Word currentWord = dictionary.getWordByIndex(currentIndex);
                Map<Word, Float> synonymsForCurrentWord = dictionary.getSynonymsForWord(currentWord);

                StringBuilder result = new StringBuilder();
                result.append(currentWord.getValue());
                result.append("=");
                for (Map.Entry<Word, Float> synonym : synonymsForCurrentWord.entrySet()) {
                    result.append(synonym.getKey().getValue() + ": " + synonym.getValue() + ", ");
                }
                if (!synonymsForCurrentWord.isEmpty()) {
                    result.delete(result.length() - 2, result.length());
                }
                result.append("\n");

                writer.write(result.toString());

                // Test
                if (currentIndex % 1000 == 0) {
                    System.out.println("Amount of checked words: " + currentIndex);
                }
            }

        } catch (IOException e) {
            System.out.println("Unable to write result into a file");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
