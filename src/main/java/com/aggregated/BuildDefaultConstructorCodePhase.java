package com.aggregated;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class BuildDefaultConstructorCodePhase extends BaseConstructorPhaseAlgorithm {
    private BuildDefaultConstructorCodePhase() {
        super(null);
    }
    public BuildDefaultConstructorCodePhase(RawClientRuleInput input) {
        super(input);
    }
    private String buildFullCtor(Class<?> clazz, String modifierKeyWord, PhaseChainedResult previousInput) {
        Map<String, String> lineBreaksNTabs = IndentationUtils.build(Optional.empty());
        StringBuilder constructorCode = new StringBuilder("");
        constructorCode
                .append(IndentationUtils.LINE_BREAK + lineBreaksNTabs.get(IndentationUtils.OUTER_BLOCK_TAB) + modifierKeyWord)
                .append(" " + clazz.getSimpleName())
                //TODO if parent already private default ctor
                //	-> abrupty return emtpy...;
                .append("() {" + IndentationUtils.LINE_BREAK + lineBreaksNTabs.get(IndentationUtils.INNER_BLOCK_TAB) + "super();");

        /**
         * To eval if already non-null field.
         */
        DefaultConstructorFieldPhaseOutput output = (DefaultConstructorFieldPhaseOutput) previousInput;
        if (Objects.nonNull(output.getFieldToDefaultValue())) {
            for (Map.Entry<Field, String> entry : output.getFieldToDefaultValue().entrySet()) {
                constructorCode.append(IndentationUtils.LINE_BREAK + lineBreaksNTabs.get(IndentationUtils.INNER_BLOCK_TAB) + "this.")
                        .append(entry.getKey().getName()).append(" = ").append(entry.getValue()).append(";");
            }
        }
        //flush
        output.reset();
        constructorCode.append(IndentationUtils.LINE_BREAK + lineBreaksNTabs.get(IndentationUtils.OUTER_BLOCK_TAB) + "}");
        return constructorCode.toString();
    }
    @Override
    public PhaseChainedResult execute(PhaseChainedResult previousInput) {
        //haiz, Class.forName auto execute static block, which runs into exception.
        String modifierKeyWord = rawInput.getAccessModifier().toString().equalsIgnoreCase("protected")
                ? Modifier.toString(Modifier.PROTECTED) : Modifier.toString(Modifier.PRIVATE);
        String constructorCode = buildFullCtor(CLAZZ, modifierKeyWord, previousInput);
        /**
         * Hmmm, sometimes don't trust reflection...
         * if it fails to detect existing default ctor..
         */
        if (CLASS_CONTENT.contains(constructorCode)) {
            return null;
        }
        StringBuilder modifiedCodeBuilder = new StringBuilder(CLASS_CONTENT.length() + constructorCode.length());
        modifiedCodeBuilder.append(CLASS_CONTENT, 0, WRITABLE_CTOR_IDX + 1);
        modifiedCodeBuilder.append("  " + constructorCode);
        modifiedCodeBuilder.append(CLASS_CONTENT, WRITABLE_CTOR_IDX + 1, CLASS_CONTENT.length());
        return new BuildConstructorPhaseOutput(modifiedCodeBuilder.toString());
    }

}














