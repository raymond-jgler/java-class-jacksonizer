package com.aggregated;

import java.util.ArrayList;
import java.util.List;

/**
 * Requires a strict class order for the chain.
 */
public class AlgorithmPhaseBuilder {
    //    private static final String[] DEFAULT_ORDER = {"ValidateClassPhase", "DefaultConstructorValidateFieldPhase", "BuildDefaultConstructorCodePhase"};
    private static final List<BaseConstructorPhaseAlgorithm> concretePhases = new ArrayList();

    public static List<BaseConstructorPhaseAlgorithm> buildConcretePhases(RawClientRuleInput rawInput,
                                                                          DecorationStrategyMode mode) {
        concretePhases.clear();
        /**
         * Annotatable constructor decoration
         */
        if (DecorationStrategyMode.ANNOTATE_PARAMERTISED_CONSTRUCTOR.equals(mode)) {
            concretePhases.add(new ValidateClassPhase(rawInput));
            concretePhases.add(new ValidateAnnotatableFieldPhase(rawInput));
            concretePhases.add(new BuildAnnotatableCodePhase(rawInput));
            concretePhases.add(new WriteConstructorResultPhase(rawInput));
        }
        //TODO, for getters, setters
        return concretePhases;
    }
}














