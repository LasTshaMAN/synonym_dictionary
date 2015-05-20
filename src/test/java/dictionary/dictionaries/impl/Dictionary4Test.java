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
public class Dictionary4Test {

    @Mock
    private Logger logger;

    @InjectMocks
    private Dictionary4 dictionary;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doNothing().when(logger).warn(any(String.class));
        doNothing().when(logger).info(any(String.class));
    }

    @Test
    public void getContents() throws Exception {
        Set<String> result = dictionary.getContents();
        assertFalse(result.contains("добавить в избранное"));
        assertFalse(result.contains("обратная связь"));
        assertFalse(result.contains("а"));
        assertFalse(result.contains("эврика!"));
        assertFalse(result.contains("я"));
        assertFalse(result.contains("экс-"));
        assertTrue(result.contains("абзац"));
        assertTrue(result.contains("авторитет"));
        assertTrue(result.contains("автохтон"));
        assertTrue(result.contains("аккомпанировать"));
        assertTrue(result.contains("атрибут"));
        assertTrue(result.contains("аэродром"));
        assertTrue(result.contains("багаж"));
        assertTrue(result.contains("баловник"));
        assertTrue(result.contains("бас"));
        assertTrue(result.contains("бытьчьей-либоработой"));
        assertTrue(result.contains("ера"));
        assertTrue(result.contains("экзаменовать"));
        assertTrue(result.contains("ящик"));
    }

    @Test
    public void getSynonymsForFirstExistingWord() throws Exception {
        String existingWord = "набег";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("атака",
                "братьнахрапом", "война", "вторжение", "вылазка", "выпад", "кампания", "нападение", "наплыв",
                "напор", "натиск", "начало", "нашествие", "облава", "охота", "приступ", "собрание", "штурм",
                "экспедиция"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForSecondExistingWord() throws Exception {
        String existingWord = "замок";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("дворец",
                "чертог", "дом", "палата", "палаты", "палаццо", "терем", "хоромы"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForThirdExistingWord() throws Exception {
        String existingWord = "каша";
        List<String> expectedSynonymsForExistingWord = new ArrayList<>(Arrays.asList("беспорядок",
                "ад", "базар", "безалаберщина", "безнарядица", "безнарядье", "беспорядица",
                "беспутица", "бестолковщина", "бестолочь", "ералаш", "замешательство", "кавардак", "кагал",
                "катавасия", "кутерьма", "нескладица", "несогласие", "нестроение", "неурядица", "неустройство",
                "передряга", "пертурбация", "путаница", "разгром", "разногласие", "разноголосица", "расстройство",
                "светопреставленье", "содом", "столпотворение", "сумятица", "сутолока", "хаос", "шабаш"));
        Set<String> result = dictionary.getSynonymsForWord(existingWord);
        assertTrue(result.containsAll(expectedSynonymsForExistingWord));
        assertEquals(expectedSynonymsForExistingWord.size(), result.size());
    }

    @Test
    public void getSynonymsForNonExistingWord() throws Exception {
        String nonExistingWord = "полужидкая";
        Set<String> result = dictionary.getSynonymsForWord(nonExistingWord);
        assertTrue(result.isEmpty());
    }
}