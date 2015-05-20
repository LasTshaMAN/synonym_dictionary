package dictionary.repositories.jdbc;

import dictionary.entities.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Primary
@Repository
public class WordRepo implements dictionary.repositories.WordRepo {

    private NamedParameterJdbcTemplate jdbcTemplate;

    private WordRowMapper wordRowMapper = new WordRowMapper();

    private SynonymRowMapper synonymRowMapper = new SynonymRowMapper();

    // For fast sequential iteration
    private int baseIndex = 0;
    private int cacheSize = 0;
    private List<Word> cachedWords = new ArrayList<>();

    // For fast synonym creation
    private final int synonymBufferSize = 10000;
    private Collection<Map<String, Object>> parametersBuffer = new ArrayList<>();

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Word getWordByIndex(int index) {
        if (!cachedWords.isEmpty() && (index >= baseIndex && index < (baseIndex + cacheSize))) {
            return cachedWords.get(index - baseIndex);
        } else {
            Map<String, Integer> parameters = new HashMap<>();
            parameters.put("index", index);
            cachedWords = jdbcTemplate.query(
                    "SELECT * FROM words ORDER BY words.id LIMIT :index, 5000",
                    parameters,
                    wordRowMapper
            );
            if (cachedWords.isEmpty()) {
                return null;
            }
            baseIndex = index;
            cacheSize = cachedWords.size();
            return cachedWords.get(0);
        }

        // Old implementation
        /*
        Map<String, Integer> parameters = new HashMap<>();
        parameters.put("index", index);
        List<Word> result = jdbcTemplate.query(
                "SELECT * FROM words ORDER BY words.value LIMIT :index, 1",
                parameters,
                wordRowMapper
        );
        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
        */
    }

    @Override
    public Word getWordByValue(String value) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("value", value);
        List<Word> result = jdbcTemplate.query(
                "SELECT * FROM words WHERE words.value = :value",
                parameters,
                wordRowMapper
        );
        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public int getWordAmount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM words",
                new HashMap<String, Object>(),
                Integer.class);
    }

    @Override
    public Map<Word, Float> getSynonymsForWord(Word word) {
        Map<String, Integer> parameters = new HashMap<>();
        parameters.put("wordId", word.getId());
        List<Map.Entry<Word, Float>> entries = jdbcTemplate.query(
                "SELECT synonyms.id AS id, synonyms.value AS value, words_synonyms.probability AS probability " +
                        "FROM words JOIN words_synonyms ON words.id = words_synonyms.word_id " +
                                "JOIN words AS synonyms ON words_synonyms.synonym_id = synonyms.id " +
                        "WHERE words.id = :wordId",
                parameters,
                synonymRowMapper
        );
        Map<Word, Float> result = new HashMap<>();
        for (Map.Entry<Word, Float> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public void saveWord(Word newWord) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", newWord.getValue());
        jdbcTemplate.update(
                "INSERT INTO words (value) VALUES (:value)",
                parameters
        );
    }

    @Override
    public void addSynonymForWord(Word word, Word synonym, Float probability) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("wordId", word.getId());
        parameters.put("synonymId", synonym.getId());
        parameters.put("probability", probability);
        jdbcTemplate.update(
                "INSERT INTO words_synonyms (word_id, synonym_id, probability) " +
                        "VALUES (:wordId, :synonymId, :probability)",
                parameters
        );
    }

    @Override
    public void changeProbabilityForSynonym(Word word, Word synonym, Float newProbability) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("wordId", word.getId());
        parameters.put("synonymId", synonym.getId());
        parameters.put("newProbability", newProbability);
        jdbcTemplate.update(
                "UPDATE words_synonyms SET probability = :newProbability " +
                        "WHERE word_id = :wordId AND synonym_id = :synonymId",
                parameters
        );
    }

    @Override
    public void deleteAllUnboundWords() {
        jdbcTemplate.update(
                "DELETE FROM words " +
                        "WHERE words.id NOT IN ( " +
                            "SELECT w_s1.word_id FROM words_synonyms AS w_s1 " +
                            "UNION ALL " +
                            "SELECT w_s2.synonym_id FROM words_synonyms AS w_s2 " +
                        ");",
                new HashMap<String, Object>()
        );
    }

    private static class WordRowMapper implements org.springframework.jdbc.core.RowMapper<Word> {
        @Override
        public Word mapRow(ResultSet resultSet, int i) throws SQLException {
            Word result = new Word();
            result.setId(resultSet.getInt("id"));
            result.setValue(resultSet.getString("value"));
            return result;
        }
    }

    private static class SynonymRowMapper implements org.springframework.jdbc.core.RowMapper<Map.Entry<Word, Float>> {
        @Override
        public Map.Entry<Word, Float> mapRow(ResultSet resultSet, int i) throws SQLException {
            final Word word = new Word();
            word.setId(resultSet.getInt("id"));
            word.setValue(resultSet.getString("value"));
            final Float probability = resultSet.getFloat("probability");

            return new Map.Entry<Word, Float>() {
                private final Word key = word;
                private Float value = probability;

                @Override
                public Word getKey() {
                    return key;
                }

                @Override
                public Float getValue() {
                    return value;
                }

                @Override
                public Float setValue(Float value) {
                    this.value = value;
                    return value;
                }
            };
        }
    }

    @Override
    public void addSynonymForWordLazily(Word word, Word synonym, Float probability) {
        if (parametersBuffer.size() > synonymBufferSize) {
            flushSynonyms();
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("wordId", word.getId());
        parameters.put("synonymId", synonym.getId());
        parameters.put("probability", probability);
        parametersBuffer.add(parameters);
    }

    @Override
    public void flushSynonyms() {
        if (!parametersBuffer.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO words_synonyms (word_id, synonym_id, probability) " +
                            "VALUES (:wordId, :synonymId, :probability)",
                    parametersBuffer.toArray(new Map[0])
            );
            parametersBuffer.clear();
        }
    }
}
