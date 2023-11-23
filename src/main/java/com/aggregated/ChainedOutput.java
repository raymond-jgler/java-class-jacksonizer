package com.aggregated;

public abstract class ChainedOutput implements PhaseChainedResult, Cloneable {
    public abstract boolean failVerify();
    public abstract void reset();
}














