package com.aggregated;

import java.util.Objects;

public class FileBasedInputReceiver extends InputReceiver {
    private FileBasedInputReceiver(){}
    public static InputReceiver buildWith(Object... objects) {
        if (Objects.isNull(objects)) {
            return new FileBasedInputReceiver();
        }
        return null; //TODO
    }
}


