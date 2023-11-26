package com.aggregated;

import java.util.Objects;

public class StringBasedInputReceiver extends InputReceiver {
    private final String PACKAGE_NAME;

    private StringBasedInputReceiver() {
        PACKAGE_NAME = null;
    }
    private StringBasedInputReceiver(String packageName) {
        super(packageName);
        PACKAGE_NAME = packageName;
    }

    public String getPackageName() {
        return PACKAGE_NAME;
    }


    public static InputReceiver buildWith(Object... objects) {
        if (Objects.isNull(objects)) {
            return new StringBasedInputReceiver();
        }
        /**
         * need to be more flexible
         */
        return new StringBasedInputReceiver(objects[0].toString());
    }

}














