package dictionary.dictionaries.impl;

import dictionary.dictionaries.Dictionary;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class Dictionary3 extends Dictionary {

    private final String baseUrl = "http://synonymonline.ru";

    @Autowired
    private Logger logger;

    @Override
    public Set<String> getContents() {
        Set<String> result = new TreeSet<>();

        for (char currentLetter = 'А'; currentLetter != ('Я' + 1); ++currentLetter) {
            result.addAll(getWordsForLetter(currentLetter));
        }
        result.addAll(getWordsForLetter('Ё'));

        return result;
    }

    @Override
    public Set<String> getSynonymsForWord(String word) {
        Set<String> result = new TreeSet<>();

        String url = baseUrl + "/" + word.substring(0, 1).toUpperCase() + "/" + word;
        Document document = getDocumentForUrl(url);
        if (document == null) {
            logger.warn("Unable to fetch synonyms for word: \"" + word + "\"");
            return result;
        }

        Elements synonymTags = document.select("div[id=content] > ol > li");
        for (Element synonymTag : synonymTags) {
            String synonymWord = tryToConvertToNormalizedWordOrPhrase(synonymTag.text());
            if (synonymWord != null) {
                result.add(synonymWord);
            }
        }

        return result;
    }

    private List<String> getWordsForLetter(char letter) {
        List<String> result = new ArrayList<>();

        String url = baseUrl + "/" + String.valueOf(letter);
        Document contentsDocument = getDocumentForUrl(url);
        if (contentsDocument == null) {
            logger.warn("Unable to connect to url: \"" + url + "\"");
        }
        boolean pageExists = true;

        while (pageExists) {
            Elements wordElements = contentsDocument.select("ul.words > li > a");
            for (Element wordElement : wordElements) {
                String word = tryToConvertToNormalizedWordOrPhrase(wordElement.text());
                if (word != null) {
                    result.add(word);
                }
            }

            Elements currentPageSpanTags = contentsDocument.select("div.pager > span");
            if (!currentPageSpanTags.isEmpty()) {
                Element nextPage = currentPageSpanTags.get(0).nextElementSibling();
                if (nextPage != null) {
                    String nextContentsUrl = baseUrl + nextPage.attr("href");
                    contentsDocument = getDocumentForUrl(nextContentsUrl);
                    if (contentsDocument == null) {
                        logger.warn("Unable to connect to url: \"" + nextContentsUrl + "\"");
                    }
                } else {
                    pageExists = false;
                }
            } else {
                pageExists = false;
            }
        }

        return result;
    }
}
