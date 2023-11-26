package com.aggregated;

public class InitialDummyPhase implements PhaseChainedResult {
    public static InitialDummyPhase emptyInstance() {
        return new InitialDummyPhase();
    }
    private InitialDummyPhase(){}
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














