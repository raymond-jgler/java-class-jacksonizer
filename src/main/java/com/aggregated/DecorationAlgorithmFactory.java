package com.aggregated;

import java.util.*;

public class DecorationAlgorithmFactory {
    public static List<DecorationAlgorithm> fromStrategyModes(Map<DecorationStrategyMode, ExecutionRule> modeToRule, InputReceiver inputReceiver, RawClientRuleInput rawInput) {
        List<DecorationAlgorithm> algorithms = new ArrayList<>();
        for (Map.Entry<DecorationStrategyMode, ExecutionRule> entry : modeToRule.entrySet()) {
            DecorationStrategyMode mode = entry.getKey();
            ExecutionRule rule = entry.getValue();
            if (Objects.nonNull(mode) && Objects.nonNull(rule)) {
                algorithms.add(fromStrategyMode(inputReceiver, mode, rule, rawInput));
            }
        }
        return Collections.unmodifiableList(algorithms);
    }
    public static DecorationAlgorithm fromStrategyMode(InputReceiver inputReceiver, DecorationStrategyMode mode, ExecutionRule executionRule, RawClientRuleInput rawInput) {
        if (DecorationStrategyMode.DEFAULT.equals(mode) || DecorationStrategyMode.DEFAULT_CONSTRUCTOR.equals(mode)) {
            return new DefaultConstructorDecorator(inputReceiver, executionRule, rawInput, mode);
        } else if (DecorationStrategyMode.ANNOTATE_PARAMERTISED_CONSTRUCTOR.equals(mode)) {
            return new AnnotatableConstructorDecorator(inputReceiver, executionRule, rawInput, mode);
        }
        return null;
    }
}














