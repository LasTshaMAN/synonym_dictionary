package dictionary.dictionaries.impl;

import dictionary.dictionaries.Dictionary;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.html.HTMLElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class Dictionary4 extends Dictionary {

    private final String baseUrl = "http://sinonimus.ru";

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

        String url = "http://sinonimus.ru/sinonim_k_slovy_" + word;
        Document document = getDocumentForUrl(url);
        if (document == null) {
            logger.warn("Unable to fetch synonyms for word: \"" + word + "\"");
            return result;
        }

        Elements synonymTags = document.select("article > section > ol > li > span");
        for (Element synonymTag : synonymTags) {
            String synonymWord = tryToConvertToNormalizedWordOrPhrase(synonymTag.text());
            if (synonymWord != null && !synonymWord.equals(word)) {
                result.add(synonymWord);
            }
        }

        return result;
    }

    private List<String> getWordsForLetter(char letter) {
        List<String> result = new ArrayList<>();

        String url = baseUrl + "/sinonimi_na_bukvu_" + String.valueOf(letter);
        Document contentsDocument = getDocumentForUrl(url);
        if (contentsDocument == null) {
            logger.warn("Unable to connect to url: \"" + url + "\"");
        }
        boolean pageExists = true;

        while (pageExists) {
            Elements wordElements = contentsDocument.select("tbody > tr > td > table[width=550] > tbody > tr > td > a");
            for (Element wordElement : wordElements) {
                String word = tryToConvertToNormalizedWordOrPhrase(wordElement.text());
                if (word != null) {
                    result.add(word);
                }
            }

            Elements currentPageSpanTags = contentsDocument.select("tbody > tr > td > a:containsOwn(следующая)");
            if (!currentPageSpanTags.isEmpty()) {
                Element nextPage = currentPageSpanTags.get(0);
                String nextContentsUrl = baseUrl + "/" + nextPage.attr("href");
                contentsDocument = getDocumentForUrl(nextContentsUrl);
                if (contentsDocument == null) {
                    logger.warn("Unable to connect to url: \"" + nextContentsUrl + "\"");
                }
            } else {
                pageExists = false;
            }
        }

        return result;
    }
}
