package com.aggregated;

import java.util.Objects;

public class ThreadUtils {
    public static void executeAndJoinAll(Thread[] threads) {
        for (Thread thread : threads) {
            if (Objects.isNull(thread)) {
                break;
            }
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                if (Objects.isNull(thread)) {
                    break;
                }
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
