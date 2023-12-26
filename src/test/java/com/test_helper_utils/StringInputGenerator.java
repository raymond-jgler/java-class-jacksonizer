package com.test_helper_utils;

import com.aggregated.IndentationUtils;
import com.aggregated.ThreadUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.aggregated.CharacterRepository.*;

public class StringInputGenerator extends BaseInputGenerator {
    private static final StringInputGenerator INSTANCE = new StringInputGenerator();
    private static final String NULL_AS_STRING = "null";
    private static final String NULL_KEYWORD = null;
    private static final String EMPTY = "";
    private static final int DEFAULT_CHARACTER_SIZE = 10;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:'\",.<>/?`~";
    private static final String ALL_CHARACTERS = LETTERS + DIGITS + SPECIAL_CHARACTERS + " ";
    public String randomWith(int length, RANDOM_TYPE... type) {
        final int availThreads = Runtime.getRuntime().availableProcessors();
        int possibleThreads = length / availThreads;
        StringBuffer chosen = new StringBuffer(chooseString(type));
        int bound = chosen.length();
        if (possibleThreads < 2 || possibleThreads > availThreads) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(chosen.charAt(RandomUtils.randomInt((bound))));
            }
            return sb.toString();
        }
        Thread[] threads = new Thread[possibleThreads];
        int threadIdx = 0;
        AtomicReference<char[]> container = new AtomicReference<>(new char[length]);
        AtomicInteger decreaser = new AtomicInteger(possibleThreads);
        AtomicInteger increaser = new AtomicInteger(0);
        for (int i = 0; i < threads.length; i++) {
            AtomicInteger idx = new AtomicInteger(increaser.get());
            AtomicInteger atomicLen = new AtomicInteger(length / decreaser.get());
            Runnable runnable = (() -> {
                for (; idx.get() < atomicLen.get() - 1;) {
                    container.get()[idx.getAndIncrement()] = chosen.charAt(RandomUtils.randomInt(bound));
                }
            });
            threads[threadIdx++] = new Thread(runnable);
            increaser.set(atomicLen.get() + 1);
            decreaser.getAndDecrement();
        }
        ThreadUtils.executeAndJoinAll(threads);
        return new String(container.get());
    }
    private String chooseString(RANDOM_TYPE ...type) {
        if (Objects.isNull(type)) {
            return ALL_CHARACTERS;
        }
        StringBuilder runner = new StringBuilder();

        for (RANDOM_TYPE eachType : type) {
            switch (eachType) {
                case ALL_CHARACTERS:
                    return ALL_CHARACTERS;
                case LETTERS:
                    runner.append(LETTERS);
                    break;
                case NUMBERS:
                    runner.append(DIGITS);
                    break;
                case SPECIAL_CHARACTERS:
                    runner.append(SPECIAL_CHARACTERS);
                    break;
            }
        }
        return runner.toString();
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
    public String blankString(int spaces) {
        if (spaces == -1) {
            spaces = 0;
        }
        return IndentationUtils.genCharsWithLen(SPACE, spaces);
    }
    public String randomWith(boolean useLetters, boolean useNumbers, int size) {
        return RandomStringUtils.random(size, useLetters, useNumbers);
    }
}
