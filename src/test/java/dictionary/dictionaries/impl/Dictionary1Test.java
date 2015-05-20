package dictionary.dictionaries.impl;

import dictionary.dictionaries.Dictionary;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Dictionary1Test {

    @Mock
    private Logger logger;

    @InjectMocks
    private Dictionary1 dictionary;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(logger).warn(any(String.class));
        doNothing().when(logger).info(any(String.class));
    }

    @Test
    public void getContents() throws Exception {
        Set<String> result = dictionary.getContents();
        assertTrue(result.contains("абрикос"));
        assertTrue(result.contains("авария на чернобыльской аэс"));
        assertTrue(result.contains("авиационная бомба"));
        assertTrue(result.contains("апл"));
        assertTrue(result.contains("аэс"));
        assertTrue(result.contains("афон"));
        assertTrue(result.contains("еж"));
        assertTrue(result.contains("ейск"));
        assertTrue(result.contains("ехидный"));
        assertTrue(result.contains("ябедничанье"));
        assertTrue(result.contains("языческий"));
        assertTrue(result.contains("язычковый музыкальный инструмент"));
    }

    @Test
    public void getSynonymsForFirstExistingWord() throws Exception {
        String existingWord = "каблучок";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("каблук",
                "каблучный", "выступ", "выступающая часть", "нижняя часть"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForSecondExistingWord() throws Exception {
        String existingWord = "замок";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("замковый",
                "замок для запирания", "замочек", "замочный", "запор", "запорное устройство",
                "замковый комплекс", "замок феодала", "средневековый замок", "дворец", "крепость",
                "оборонительное сооружение"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForThirdExistingWord() throws Exception {
        String existingWord = "каша";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("кушанье",
                "жижа", "кашица", "месиво", "тестообразная масса"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForNonExistingWord() throws Exception {
        String nonExistingWord = "гаша";
        Set<String> result = dictionary.getSynonymsForWord(nonExistingWord);
        assertTrue(result.isEmpty());
    }
}