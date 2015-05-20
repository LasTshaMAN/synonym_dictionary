package dictionary.dictionaries;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Dictionary {

    private final int MAX_QUERY_AMOUNT = 5;

    private Pattern phrasePattern = Pattern.compile("[а-яё]+[а-яё\\s-]*[а-яё]");

    /**
     * Returns list of lowercase words that dictionary contains;
     */
    public abstract Set<String> getContents();

    /**
     * Returns list of lowercase synonyms for a given word that dictionary contains;
     */
    public abstract Set<String> getSynonymsForWord(String word);

    protected Document getDocumentForUrl(String url) {
        Document result = null;

        int counter = 0;
        while (counter < MAX_QUERY_AMOUNT) {
            try {
                result = Jsoup.connect(url).get();
                break;

            } catch (IOException e) {
                // try {
                //     Thread.sleep(3000);
                // } catch (InterruptedException exception) {
                //     Thread.currentThread().interrupt();
                // }
            }
            ++counter;
        }

        return result;
    }

    /**
     * Returns "input" string converted to lowercase if input string is valid word of phrase
     * Otherwise returns "null"
     */
    protected String tryToConvertToNormalizedWordOrPhrase(String input) {
        String result = null;

        input = input.toLowerCase();
        Matcher matcher = phrasePattern.matcher(input);
        if (matcher.matches()) {
            result = input;
        }

        return result;
    }

    /**
     * Returns list of words and phrases found inside "input" string
     */
    protected List<String> extractNormalizedWordsOrPhrases(String input) {
        List<String> result = new ArrayList<>();

        input = input.toLowerCase();
        Matcher matcher = phrasePattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }
}
