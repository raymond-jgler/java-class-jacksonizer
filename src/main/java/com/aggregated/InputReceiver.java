













package com.aggregated;

import java.util.Arrays;
import java.util.List;

public abstract class InputReceiver {
    protected List<Object> values;
    public static final String BASE_SOURCE = "src\\main\\java\\";
    protected InputReceiver(Object... values) {
        this.values = Arrays.asList(values);
    }
    public List<Object> getValues() {
        return this.values;
    }
    public static final InputReceiver STRING_BASED_INPUT = StringBasedInputReceiver.buildWith(null);
    public static final InputReceiver FILE_BASED_INPUT = FileBasedInputReceiver.buildWith(null);

}
