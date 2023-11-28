package com.aggregated;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

}