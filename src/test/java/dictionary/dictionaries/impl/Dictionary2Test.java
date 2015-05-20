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
public class Dictionary2Test {

    @InjectMocks
    private Dictionary2 dictionary;

    @Mock
    private Logger logger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(logger).warn(any(String.class));
        doNothing().when(logger).info(any(String.class));
    }

    @Test
    public void getContents() throws Exception {
        Set<String> result = dictionary.getContents();
        assertFalse(result.contains("-ум"));
        assertFalse(result.contains("Z-образный"));
        assertFalse(result.contains("а волны и стонут, и плачут"));
        assertFalse(result.contains("авар."));
        assertFalse(result.contains("авто-"));
        assertFalse(result.contains("я"));
        assertTrue(result.contains("а вдруг"));
        assertTrue(result.contains("абажур"));
        assertTrue(result.contains("абаим"));
        assertTrue(result.contains("абонентка"));
        assertTrue(result.contains("абхазско-северокавказский"));
        assertTrue(result.contains("именной"));
        assertTrue(result.contains("имитационный"));
        assertTrue(result.contains("опора-стойка"));
        assertTrue(result.contains("оповестивший"));
        assertTrue(result.contains("юфта"));
        assertTrue(result.contains("ястак"));
    }

    @Test
    public void getSynonymsForFirstExistingWord() throws Exception {
        String existingWord = "каблучок";
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getSynonymsForSecondExistingWord() throws Exception {
        String existingWord = "замок";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("дворец",
                "крепость", "тюрьма", "сооружение", "здание", "жильё", "запор", "замочек", "уключина", "устройство",
                "механизм", "приспособление", "защита", "способ", "замковый камень"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForThirdExistingWord() throws Exception {
        String existingWord = "каша";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("месиво", "жижа", "шмяка",
                "шуга", "беспорядок", "неразбериха", "путаница", "еда", "кушанье", "субстанция", "состояние"));
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