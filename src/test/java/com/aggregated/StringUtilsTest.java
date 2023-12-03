package com.aggregated;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest {

    @Test
    public void testIsNonBlankWithBlankString() {
        assertFalse(StringUtils.isNoneBlank(""));
    }

    @Test
    public void testIsNonBlankWittNullString() {
        assertFalse(StringUtils.isNoneBlank(null));
    }

    @Test
    public void testIsNoneBlankWithOnlyWhitespaceString() {
        assertFalse(StringUtils.isNoneBlank("    "));
    }

    @Test
    public void testIsNoneBlankWithLetterOrDigitString() {
        assertTrue(StringUtils.isNoneBlank("abc123"));
    }

    @Test
    public void testIsNoneBlankWithSpecialCharacters() {
        assertTrue(StringUtils.isNoneBlank("abc!@#"));
    }

    @Test
    public void testIsNoneBlankWithMixedCharacters() {
        assertTrue(StringUtils.isNoneBlank("abc 123"));
    }
    @Test
    public void testDummy() {
        int length = 10;
        boolean useLetters = true;
        boolean useNumbers = false;
        String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);

        final Function<String, Boolean> testNoneBlank = StringUtils::isNoneBlank;
        assertTrue(testNoneBlank.apply("@abc!@#"));
    }

}