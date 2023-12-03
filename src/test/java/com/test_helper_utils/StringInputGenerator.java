package com.test_helper_utils;

import com.aggregated.IndentationUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Optional;
import java.util.Random;

public class StringInputGenerator extends BaseInputGenerator {
    private static final StringInputGenerator INSTANCE = new StringInputGenerator();
    private static final String NULL_AS_STRING = "null";
    private static final String NULL_KEYWORD = null;
    private static final String EMPTY = "";
    private static final char SPACE = ' ';
    private static final int DEFAULT_CHARACTER_SIZE = 10;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?`~";
    private static final String ALL_CHARACTERS = LETTERS + DIGITS + SPECIAL_CHARACTERS + " ";
    private static final Random random = new Random();

    public static String randomWith(int length) {
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            char randomChar = ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length()));
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }
    private StringInputGenerator() {}

    public static StringInputGenerator current() {
        return INSTANCE;
    }
    public String nullObject() {
        return NULL_KEYWORD;
    }
    public String nullAsString() {
        return NULL_AS_STRING;
    }
    public String blankString(Optional<Integer> spaces) {
        if (!spaces.isPresent()) {
            spaces = Optional.of(1);
        }
        return IndentationUtils.genCharsWithLen(SPACE, spaces.get());
    }
    public String randomWith(Optional<Boolean> useLetters, Optional<Boolean> useNumbers, Optional<Integer> size) {
        if (!size.isPresent()) {
            size = Optional.of(DEFAULT_CHARACTER_SIZE);
        }
        if (!useLetters.isPresent()) {
            useLetters = Optional.of(Boolean.TRUE);
        }
        if (!useNumbers.isPresent()) {
            useNumbers = Optional.of(Boolean.TRUE);
        }
        return RandomStringUtils.random(size.get(), useLetters.get(), useNumbers.get());
    }
}
