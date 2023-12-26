package com.main_logic_tests.string_utils;

import com.aggregated.StringUtils;
import com.test_helper_utils.*;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest_IsNoneBlank {
    private static final Logger LOG = LoggerFactory.getLogger(StringUtilsTest_IsNoneBlank.class);
    private static final Function<String, Boolean> LOGIC = StringUtils::isNoneBlank;
    private static final StringInputGenerator MOUNTED_STRING_MODE = (StringInputGenerator) TestInputGeneratorContainer.current().generate(GENERATOR_TYPE.STRING);
    /** 10 million */
    private static final int randomBound = 10000000;
    private static final int repeatTimes = 17;
    @RepeatedTest(value = repeatTimes)
    public void shouldHandleBlankString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.blankString(RandomUtils.randomInt(randomBound)));
        assertTrue(Boolean.FALSE == action);
    }
    @Test
    public void shouldHandleNull_OrNullAsStrings() {
        final boolean action01 = LOGIC.apply(MOUNTED_STRING_MODE.nullAsString());
        final boolean action02 = LOGIC.apply(MOUNTED_STRING_MODE.nullObject());
        assertTrue( Boolean.TRUE == action01);
        assertTrue( Boolean.FALSE == action02);
    }

    @RepeatedTest(value = repeatTimes)
    public void shouldHandleOnlyWhitespaceString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.blankString(RandomUtils.randomInt(randomBound)));
        assertTrue(Boolean.FALSE == action);
    }
    @RepeatedTest(value = repeatTimes)
    public void shouldHandleLetterOrDigitString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.randomWith(randomBound, RANDOM_TYPE.LETTERS, RANDOM_TYPE.NUMBERS));
        assertTrue(Boolean.TRUE == action);
    }
    @RepeatedTest(value = repeatTimes)
    public void shouldHandleMixedCharacters() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.randomWith(RandomUtils.randomInt(randomBound), RANDOM_TYPE.ALL_CHARACTERS));
        assertTrue(Boolean.TRUE == action);
    }
}