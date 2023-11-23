













package com.aggregated;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DefaultConstructorValidateFieldPhase extends BaseConstructorPhaseAlgorithm {
    private final Map<Field, String> fieldToDefaultValue = new HashMap<>();
    private DefaultConstructorValidateFieldPhase() {
        super( null);
    }
    public DefaultConstructorValidateFieldPhase(RawClientRuleInput rawInput) {
        super(rawInput);
    }
    @Override
    public PhaseChainedResult execute(PhaseChainedResult previousInput) {
        /**
         * To eval if already non-null field.
         */
        int newFrom = CLASS_CONTENT.indexOf("{", CLASS_KEYWORD_N_NAME_IDX);
        int newTo = CLASS_CONTENT.indexOf(";", WRITABLE_CTOR_IDX);
        String[] searchRange = CLASS_CONTENT.substring(newFrom, newTo).split(" ");
        for (Field field : CLAZZ.getDeclaredFields()) {
            int fieldModifiers = field.getModifiers();
            if (Modifier.isStatic(fieldModifiers) || !Modifier.isFinal(fieldModifiers) || field.getName().contains("this$") || (Modifier.isFinal(fieldModifiers) && false == isFieldUnassigned(field.getName(), searchRange))) {
                continue;
            }
            fieldToDefaultValue.putIfAbsent(field, getDefaultValueForPrimitiveType(field.getType()));
        }
        ValidateClassPhaseOutput prevClassPhaseOutput = (ValidateClassPhaseOutput) previousInput;
        prevClassPhaseOutput.reset();
        return new DefaultConstructorFieldPhaseOutput(fieldToDefaultValue);
    }

    private String getDefaultValueForPrimitiveType(Class<?> fieldType) {
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            return "Boolean.parseBoolean(null)";
        } else if (fieldType == char.class || fieldType == Character.class) {
            return "'\u0000'";
        } else if (fieldType.isPrimitive()) {
            return "0";
        } else {
            return "null";
        }
    }

    private boolean isFieldUnassigned(String fieldName, String[] searchRange) {
        for (int i = 0, n = searchRange.length; i < n; i++) {
            String running = searchRange[i];
            if (running.contains("/**") || running.contains("//") || running.contains("/*")) {
                //skip cmt block
//        for (i += 1; i < n && (!searchRange[i].contains("//") && !searchRange[i].contains("*/")); i++) {}
                for (i += 1; i < n && !searchRange[i].contains("*/") && !searchRange[i].contains("\n"); i++) {}
                i++;
                continue;
            }
            if (running.contains(fieldName)) {
                if (false == StringUtils.resolveReplaces(running, "\r", "", "\n", "", " ", "").equals(fieldName)) {
                    continue;
                }
                if (running.contains(";")) {
                    return true;
                }
                if (running.contains("=")) { //fishy check
                    return false;
                }
                for (; i < n; i++) { //ok we'll take it from here then bail.
                    running = searchRange[i];
                    if (running.contains("=")) {
                        return false;
                    }
                    if (running.contains(";")) {
                        break;
                    }
                }
            }
        }
        return true;
    }

}
