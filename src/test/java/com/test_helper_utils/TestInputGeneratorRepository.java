package com.test_helper_utils;

import java.util.Objects;

public class TestInputGeneratorRepository {
    private static final BaseInputGenerator STRING_MODE_GENERATOR = StringInputGenerator.current();
    private static final BaseInputGenerator DEFAULT_MODE = STRING_MODE_GENERATOR;
    private static final TestInputGeneratorRepository INSTANCE = new TestInputGeneratorRepository();
    public static final MODE STRING = MODE.STRING;
    private TestInputGeneratorRepository(){}
    public static TestInputGeneratorRepository current() {
        return INSTANCE;
    }
    public BaseInputGenerator get(MODE mode) {
        BaseInputGenerator generator = null;
        switch (mode) {
            case STRING: {
                generator = STRING_MODE_GENERATOR;
                break;
            }
            default: {
                //
                break;
            }
        }
        if (Objects.isNull(generator)) {
            generator = DEFAULT_MODE;
        }
        return generator;
    }
     private enum MODE {
        STRING
    }
}
