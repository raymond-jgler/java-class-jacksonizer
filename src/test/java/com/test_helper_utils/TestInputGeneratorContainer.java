package com.test_helper_utils;

import java.util.Objects;

public class TestInputGeneratorContainer {
    private static final BaseInputGenerator STRING_MODE_GENERATOR = StringInputGenerator.current();
    private static final BaseInputGenerator DEFAULT_MODE = STRING_MODE_GENERATOR;
    private static final TestInputGeneratorContainer INSTANCE = new TestInputGeneratorContainer();
    private TestInputGeneratorContainer(){}
    public static TestInputGeneratorContainer current() {
        return INSTANCE;
    }
    public BaseInputGenerator generate(GENERATOR_TYPE type) {
        BaseInputGenerator generator = null;
        if (GENERATOR_TYPE.STRING.equals(type)) {
            generator = STRING_MODE_GENERATOR;
        } else {
            //
        }
        if (Objects.isNull(generator)) {
            generator = DEFAULT_MODE;
        }
        return generator;
    }
}
