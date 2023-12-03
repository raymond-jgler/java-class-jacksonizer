package com.main_logic_tests.string_utils;

import com.aggregated.StringUtils;
import com.test_helper_utils.StringInputGenerator;
import com.test_helper_utils.TestInputGeneratorRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest_IsNoneBlank {
    private static final Function<String, Boolean> LOGIC = StringUtils::isNoneBlank;
    private static final StringInputGenerator MOUNTED_STRING_MODE = (StringInputGenerator) TestInputGeneratorRepository.current().get(TestInputGeneratorRepository.STRING);
    @Test
    public void shouldHandleBlankString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.blankString(Optional.of(0)));
        assertTrue(Boolean.FALSE == action);
    }
    @Test
    public void shouldHandleNull_OrNullAsStrings() {
        final boolean action01 = LOGIC.apply(MOUNTED_STRING_MODE.nullAsString());
        final boolean action02 = LOGIC.apply(MOUNTED_STRING_MODE.nullObject());
        assertTrue( Boolean.TRUE == action01);
        assertTrue( Boolean.FALSE == action02);
    }

    @Test
    public void shouldHandleOnlyWhitespaceString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.blankString(Optional.of(10)));
        assertTrue(Boolean.FALSE == action);
    }
    @Test
    public void shouldHandleLetterOrDigitString() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.randomWith(
                Optional.of(Boolean.TRUE),
                                Optional.of(Boolean.TRUE),
                                Optional.of(15)
                ));

        assertTrue(Boolean.TRUE == action);
    }
    @Test
    public void shouldHandleMixedCharacters() {
        final boolean action = LOGIC.apply(MOUNTED_STRING_MODE.randomWith(5));
        assertTrue(Boolean.TRUE == action);
    }

}