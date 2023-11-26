package com.aggregated;

public interface PhaseChainedResult extends Cloneable {
    boolean failVerify();
    ChainedOutput getValues();
    Object getRawValues();
}














