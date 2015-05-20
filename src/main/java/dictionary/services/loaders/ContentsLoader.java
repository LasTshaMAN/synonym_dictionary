package dictionary.services.loaders;

import dictionary.dictionaries.Dictionary;
import dictionary.entities.Word;
import dictionary.repositories.WordRepo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ContentsLoader implements Runnable {

    @Autowired
    private WordRepo wordRepo;

    @Autowired
    private Dictionary dictionary;

    @Autowired
    private Logger logger;

    @Transactional
    @Override
    public void run() {
        Set<String> words = dictionary.getContents();
        for (String word : words) {
            Word newWord = new Word();
            newWord.setValue(word);
            wordRepo.saveWord(newWord);
        }
        logger.info("Word loading completed!");
        System.out.println("Word loading completed!");
    }
}
