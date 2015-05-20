package dictionary.services.exporters;

import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class SelectedWordsExporter {

    private final ObjectMapper MAPPER = new ObjectMapper();

    private final String OUTPUT_FILE_PATH = "result/output.json";

    @Autowired
    private WordRepo dictionary;

    public void exportSelectedWords(List<String> words) {
        Map<String, Map<String, Float>> result = new HashMap<>();

        for (String value : words) {
            Word word = dictionary.getWordByValue(value);
            if (word != null) {
                Map<Word, Float> synonyms = dictionary.getSynonymsForWord(word);
                List<Map.Entry<Word, Float>> sortedSynonyms = new ArrayList<>(synonyms.entrySet());
                sortedSynonyms.sort(new Comparator<Map.Entry<Word, Float>>() {
                    @Override
                    public int compare(Map.Entry<Word, Float> o1, Map.Entry<Word, Float> o2) {
                        if (o1.getValue() < o2.getValue()) {
                            return 1;
                        } else if (o1.getValue() > o2.getValue()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                Map<String, Float> synonymsValues = new LinkedHashMap<>();
                for (Map.Entry<Word, Float> synonym : sortedSynonyms) {
                    synonymsValues.put(synonym.getKey().getValue(), synonym.getValue());
                }
                result.put(value, synonymsValues);
            }
        }

        try {
            MAPPER.writeValue(new File(OUTPUT_FILE_PATH), result);

        } catch (Exception e) {
            System.out.println("Unable to write result into a file");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
