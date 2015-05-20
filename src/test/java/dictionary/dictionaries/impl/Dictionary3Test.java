package dictionary.dictionaries.impl;

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

@RunWith(MockitoJUnitRunner.class)
public class Dictionary3Test {

    @Mock
    private Logger logger;

    @InjectMocks
    private Dictionary3 dictionary;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(logger).warn(any(String.class));
        doNothing().when(logger).info(any(String.class));
    }

    @Test
    public void getContents() throws Exception {
        Set<String> result = dictionary.getContents();
        assertFalse(result.contains("а"));
        assertFalse(result.contains("б"));
        assertTrue(result.contains("абенакиит-се"));
        assertTrue(result.contains("абрикосовый"));
        assertTrue(result.contains("абрикотин"));
        assertTrue(result.contains("ав"));
        assertTrue(result.contains("аяк-кап"));
        assertTrue(result.contains("аятолла"));
        assertTrue(result.contains("бабки"));
        assertTrue(result.contains("бабка-ёжка"));
        assertTrue(result.contains("ёга"));
        assertTrue(result.contains("ёрш"));
        assertTrue(result.contains("йошкар-ола"));
        assertTrue(result.contains("яровит"));
        assertTrue(result.contains("яя"));
    }

    @Test
    public void getSynonymsForFirstExistingWord() throws Exception {
        String existingWord = "абажур";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("светильник",
                "колпак", "плафон", "абажурчик"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForSecondExistingWord() throws Exception {
        String existingWord = "замок";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("бурзамок",
                "дворец", "запор", "чертог", "электрозамок", "гидрозамок", "замочек", "замычка", "крепость", "алькасар",
                "бург", "мультилок", "шифрозамок", "контролька", "серьга", "едикуль"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForThirdExistingWord() throws Exception {
        String existingWord = "каша";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("гранола",
                "саламат", "путаница", "разноголосица", "беспорядок", "смесь", "кашка", "мешанина",
                "месиво", "перловка", "пшенка", "манка", "винегрет", "неразбериха", "сумятица",
                "сумбур", "хаос", "ералаш", "окрошка", "смешение", "кушанье", "кашица", "кутья",
                "мамалыга", "овсянка", "размазня", "кулеш", "кисель", "жижа", "жижица", "полужидкая масса",
                "сечка", "ячменка", "гороховица", "вараховица", "ерлы"));
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