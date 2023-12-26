package com.aggregated;

public class InitialDummyPhase implements PhaseChainedResult {
    private static final InitialDummyPhase INSTANCE = new InitialDummyPhase();
    public static InitialDummyPhase instance() {
        return INSTANCE;
    }

    private InitialDummyPhase() {
    }

    @Override
    public boolean failVerify() {
        return false;
    }

    @Override
    public ChainedOutput getValues() {
        return null;
    }

    @Override
    public Object getRawValues() {
        return null;
    }
}














