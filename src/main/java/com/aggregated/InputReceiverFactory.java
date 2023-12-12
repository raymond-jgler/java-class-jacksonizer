package com.aggregated;

public class InputReceiverFactory {
    public static InputReceiver fromValues(InputReceiver receiver, Object... values) {
        if (receiver instanceof StringBasedInputReceiver) {
            return StringBasedInputReceiver.buildWith(values);
        }
        return null;
    }
}











