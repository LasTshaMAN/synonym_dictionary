package dictionary.dictionaries.impl;

import dictionary.dictionaries.Dictionary;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Dictionary1 extends Dictionary {

    private final String baseUrl = "http://labinform.ru/pub/ruthes";

    private char currentLetter;

    private Map<String, String> cacheWordUrl;

    private Pattern pattern = Pattern.compile("(?<=../../../).*");

    @Autowired
    private Logger logger;

    @Override
    public Set<String> getContents() {
        Set<String> result = new TreeSet<>();

        String url = baseUrl + "/" + "index.htm";
        Document contentsDocument = getDocumentForUrl(url);
        if (contentsDocument == null) {
            logger.warn("Unable to connect to url: \"" + url + "\"");
        }

        Elements letters = contentsDocument.select("div.letter a");
        for (Element letter : letters) {
            url = baseUrl + "/" + letter.attr("href");
            Document docForLetter = getDocumentForUrl(url);
            if (docForLetter == null) {
                logger.warn("Unable to connect to url: \"" + url + "\"");
                continue;
            }

            // Scrape first letter group
            result.addAll(scrapeDocument(docForLetter));

            // Scrape the rest letter groups
            Elements letterGroups = docForLetter.select("div.top2-letters > div.letter2 a");
            for (Element letterGroup : letterGroups) {
                String parsedUrl = extractUrl(letterGroup.attr("href"));
                if (parsedUrl != null) {
                    url = baseUrl + "/" + parsedUrl;
                    Document docForLetterGroup = getDocumentForUrl(url);
                    if (docForLetterGroup != null) {
                        result.addAll(scrapeDocument(docForLetterGroup));
                    } else {
                        logger.warn("Unable to connect to url: \"" + url + "\"");
                    }
                } else {
                    logger.error("Error during parsing document on url: " +
                            baseUrl + letter.attr("href"));
                    System.exit(1);
                }
            }
        }

        return result;
    }

    @Override
    public Set<String> getSynonymsForWord(String word) {
        Set<String> result = new TreeSet<>();

        if (word.charAt(0) != currentLetter) {
            currentLetter = word.charAt(0);
            cacheWordUrl = new HashMap<>();

            // Populate cache
            String url = baseUrl + "/" + "index.htm";
            Document contentsDocument = getDocumentForUrl(url);
            if (contentsDocument == null) {
                logger.warn("Unable to connect to url: \"" + url + "\"");
            }

            Elements letters = contentsDocument.select("div.letter a");
            for (Element letter : letters) {
                if (letter.text().toLowerCase().charAt(0) == currentLetter) {
                    url = baseUrl + "/" + letter.attr("href");
                    Document docForLetter = getDocumentForUrl(url);
                    if (docForLetter == null) {
                        logger.warn("Unable to connect to url: \"" + url + "\"");
                        break;
                    }

                    // Scrape first letter group
                    cacheWordUrl.putAll(scrapeDocumentForUrls(docForLetter));

                    // Scrape the rest letter groups
                    Elements letterGroups = docForLetter.select("div.top2-letters > div.letter2 a");
                    for (Element letterGroup : letterGroups) {
                        String parsedUrl = extractUrl(letterGroup.attr("href"));
                        if (parsedUrl != null) {
                            url = baseUrl + "/" + parsedUrl;
                            Document docForLetterGroup = getDocumentForUrl(url);
                            if (docForLetterGroup != null) {
                                cacheWordUrl.putAll(scrapeDocumentForUrls(docForLetterGroup));
                            } else {
                                logger.warn("Unable to connect to url: \"" + url + "\"");
                            }
                        } else {
                            logger.error("Error during parsing document on url: " +
                                    baseUrl + letter.attr("href"));
                        }
                    }

                    break;
                }
            }
        }

        String offsetWordUrl = cacheWordUrl.get(word);
        if (offsetWordUrl == null) {
            return result;
        }
        String wordUrl = extractUrl(offsetWordUrl);
        if (wordUrl != null) {
            String url = baseUrl + "/" + wordUrl;
            Document docForWord = getDocumentForUrl(url);
            if (docForWord != null) {
                // Extract synonyms
                Elements elements = docForWord.select("div.te-info > div.conc-info > div.te-for-conc > a");
                for (Element element : elements) {
                    String synonym = tryToConvertToNormalizedWordOrPhrase(element.text());
                    if (synonym != null && !synonym.equals(word)) {
                        result.add(synonym);
                    }
                }

                // Extract hypernyms
                elements = docForWord.select("div.te-info > div.conc-info > div.conc-rels > span.rel-item");
                for (Element element : elements) {
                    Elements spans = element.getElementsByClass("rel-name");
                    for (Element span : spans) {
                        if (span.text().equals("ВЫШЕ")) {
                            Element sibling = span.nextElementSibling();
                            List<String> synonyms = extractNormalizedWordsOrPhrases(sibling.text());
                            for (String synonym : synonyms) {
                                if (!synonym.equals(word)) {
                                    result.add(synonym);
                                }
                            }
                        }
                    }
                }
            } else {
                logger.warn("Unable to fetch synonyms for word: \"" + word + "\"");
            }
        } else {
            logger.error("Error during extracting url for word: " + word);
        }

        return result;
    }

    private List<String> scrapeDocument(Document document) {
        List<String> result = new ArrayList<>();

        Elements wordElements = document.select("div.te-block > div.te-item > a");
        for (Element wordElement : wordElements) {
            String word = tryToConvertToNormalizedWordOrPhrase(wordElement.text());
            if (word != null) {
                result.add(word);
            }
        }

        return result;
    }

    private Map<String, String> scrapeDocumentForUrls(Document document) {
        Map<String, String> result = new HashMap<>();

        Elements wordElements = document.select("div.te-block > div.te-item > a");
        for (Element wordElement : wordElements) {
            String word = tryToConvertToNormalizedWordOrPhrase(wordElement.text());
            if (word != null) {
                result.put(word, wordElement.attr("href"));
            }
        }

        return result;
    }

    private String extractUrl(String offsetUrl) {
        Matcher matcher = pattern.matcher(offsetUrl);
        if (matcher.find()) {
            return matcher.group();

        } else {
            return null;
        }
    }
}
