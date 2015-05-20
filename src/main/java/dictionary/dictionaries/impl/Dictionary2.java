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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Dictionary2 extends Dictionary {

    private final String baseUrl = "https://ru.wiktionary.org";

    private final String contentsBaseUrl = "https://ru.wiktionary.org/wiki/Категория:Русский_язык";

    private final String synonymsBaseUrl = "https://ru.wiktionary.org/wiki";

    @Autowired
    private Logger logger;

    @Override
    public Set<String> getContents() {
        Set<String> result = new TreeSet<>();

        Document contentsDocument = getDocumentForUrl(contentsBaseUrl);
        if (contentsDocument == null) {
            logger.warn("Unable to connect to url: \"" + contentsBaseUrl + "\"");
        }

        boolean pageExists = true;
        while (pageExists) {
            Elements wordElements = contentsDocument.select("div#mw-pages > div.mw-content-ltr > div.mw-category > div.mw-category-group > ul > li > a");
            for (Element wordElement : wordElements) {
                if (DoesNotContainUppercaseLetter(wordElement.text())) {
                    String word = tryToConvertToNormalizedWordOrPhrase(wordElement.text());
                    if (word != null) {
                        result.add(word);
                    }
                }
            }

            Elements nextPage = contentsDocument.select("div#mw-pages > a:containsOwn(Следующая страница)");
            if (!nextPage.isEmpty()) {
                String nextContentsUrl = baseUrl + nextPage.get(0).attr("href");
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

    @Override
    public Set<String> getSynonymsForWord(String word) {
        Set<String> result = new TreeSet<>();

        String url = synonymsBaseUrl + "/" + word;
        Document document = getDocumentForUrl(url);
        if (document == null) {
            logger.warn("Unable to fetch synonyms for word: \"" + word + "\"");
            return result;
        }

        Elements tags = document.select("h1 > span:containsOwn(Русский)");
        if (tags.isEmpty()) {
            return result;
        }
        Element tag = tags.get(0).parent();

        do {
            // Find synonyms
            do {
                tag = tag.nextElementSibling();
            } while (tag != null && !tag.tagName().equals("h1") && tag.select("span:containsOwn(Синонимы)").isEmpty());
            if (tag == null || tag.tagName().equals("h1")) {
                return result;
            } else {
                tag = tag.nextElementSibling();
                if (tag.tagName().equals("ol")) {
                    Elements synonymList = tag.children();
                    for (Element synonyms : synonymList) {
                        for (Element synonym : synonyms.children()) {
                            if (synonym.tagName().equals("a")) {
                                String synonymWord = tryToConvertToNormalizedWordOrPhrase(synonym.text());
                                if (synonymWord != null && !result.contains(synonymWord)) {
                                    result.add(synonymWord);
                                }
                            }
                        }
                    }
                }
            }

            // Find hyperons
            do {
                tag = tag.nextElementSibling();
            } while (tag != null && !tag.tagName().equals("h1") && tag.select("span:containsOwn(Гиперонимы)").isEmpty());
            if (tag == null || tag.tagName().equals("h1")) {
                return result;
            } else {
                tag = tag.nextElementSibling();
                if (tag.tagName().equals("ol")) {
                    Elements hyList = tag.children();
                    for (Element hys : hyList) {
                        for (Element hy : hys.children()) {
                            if (hy.tagName().equals("a")) {
                                String hyWord = tryToConvertToNormalizedWordOrPhrase(hy.text());
                                if (hyWord != null && !result.contains(hyWord)) {
                                    result.add(hyWord);
                                }
                            }
                        }
                    }
                }
            }

        } while (true);
    }

    private boolean DoesNotContainUppercaseLetter(String word) {
        return word.equals(word.toLowerCase());
    }
}
