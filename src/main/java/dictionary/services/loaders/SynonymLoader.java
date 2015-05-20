package dictionary.services.loaders;

import dictionary.dictionaries.Dictionary;
import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class SynonymLoader implements Runnable {

    @Autowired
    private WordRepo wordRepo;

    @Autowired
    private Dictionary dictionary;

    private final String TMP_CONFIG_PATH;

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private Logger logger;

    public SynonymLoader(String contextName) {
        this.TMP_CONFIG_PATH = "result/tmp_configs/tmp_config_for_" +
                contextName.substring(0, contextName.indexOf(".")) + ".json";
    }

    @Transactional
    @Override
    public void run() {
        int currentIndex = getStartingIndex();
        int dictionarySize = wordRepo.getWordAmount();

        while (currentIndex < dictionarySize && !Thread.currentThread().isInterrupted()) {
            loadSynonymsForCurrentWordInDatabase(currentIndex);
            ++currentIndex;
        }

        if (currentIndex == dictionarySize) {
            logger.info("Synonym loading completed!");
            System.out.println("Synonym loading completed!");
            logger.info("Cleaning database of isolated words...");
            System.out.println("Cleaning database of isolated words...");
            wordRepo.deleteAllUnboundWords();
            logger.info("Cleaning completed successfully!");
            System.out.println("Cleaning completed successfully!");
            eraseIndex();
        } else {
            logger.info("Stopped loading synonyms");
            System.out.println("Stopped loading synonyms");
            logger.info("Amount of checked words: " + (currentIndex + 1));
            System.out.println("Amount of checked words: " + (currentIndex + 1));
            putNextStartingIndex(currentIndex);
        }
    }

    private void loadSynonymsForCurrentWordInDatabase(int currentIndex) {
        Word word = wordRepo.getWordByIndex(currentIndex);
        Set<String> synonymValues = dictionary.getSynonymsForWord(word.getValue());

        for (String synonymValue : synonymValues) {
            Word synonym = wordRepo.getWordByValue(synonymValue);
            if (synonym != null) {
                wordRepo.addSynonymForWord(word, synonym, null);
            } else {
                logger.info("Word dictionary doesn't contain word: \"" + synonymValue + "\"");
            }
        }
    }

    private int getStartingIndex() {
        int result = 0;

        File tmpConfigFile = new File(TMP_CONFIG_PATH);
        if (tmpConfigFile.exists()) {
            try {
                Map<String, String> keyValueMap = MAPPER.readValue(tmpConfigFile, HashMap.class);
                result = Integer.parseInt(keyValueMap.get("index"));

            } catch (Exception e) {
                logger.error("Couldn't read \"" + TMP_CONFIG_PATH + "\" file");
                e.printStackTrace();
                System.exit(1);
            }
        }
        return result;
    }

    private void putNextStartingIndex(int index) {
        Map<String, String> keyValueMap = new HashMap<>();
        keyValueMap.put("index", String.valueOf(index));
        try {
            MAPPER.writeValue(new File(TMP_CONFIG_PATH), keyValueMap);

        } catch (Exception e) {
            logger.error("Exception during writing new starting index in \"" + TMP_CONFIG_PATH + "\" file");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void eraseIndex() {
        File file = new File(TMP_CONFIG_PATH);
        file.delete();
    }
}
