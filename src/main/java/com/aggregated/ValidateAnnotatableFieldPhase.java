













package com.aggregated;

import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;

public class ValidateAnnotatableFieldPhase extends BaseConstructorPhaseAlgorithm {
  private CustomSerializationCollectedField collectedField;

  protected ValidateAnnotatableFieldPhase(RawClientRuleInput rawInput) {
    super(rawInput);
  }
  @Override
  public PhaseChainedResult execute(PhaseChainedResult previousInput) {
    ValidateClassPhaseOutput previousOutput = (ValidateClassPhaseOutput) previousInput;
    Objects.requireNonNull(previousOutput.getRawValues());
    previousOutput.reset();

    String type = "";
    if (rawInput.isCustomSerialization()) {
        type = rawInput.getCustomSerRequiredFieldType();
    }
    buildList(type);
    AnnotatableConstructorFieldPhaseOutput result = new AnnotatableConstructorFieldPhaseOutput(collectedField);
    try {
      pushStack((PhaseChainedResult) result.clone());
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    return result;
  }
  private void buildList(String withType) {
    if (Objects.isNull(this.collectedField)) {
      this.collectedField = new CustomSerializationCollectedField();
    }
    for (Field field : CLAZZ.getDeclaredFields()) {
      if (field.getName().contains("this") || field.getType().isAssignableFrom(CLAZZ)) {
        continue;
      }
      if (isSerializableField(field)) {
        this.collectedField.addSerializableField(ReflectionUtils.createDecoLocalFieldFrom(field));
      }
      if (StringUtils.isEmpty(withType)) {
        continue;
      }
      String toEvalBy = field.getType().getSimpleName();
      if (withType.contains(".")) {
        toEvalBy = field.getType().getName();
      }
      if (toEvalBy.equalsIgnoreCase(withType)) {
        this.collectedField.addCustomRequiredField(ReflectionUtils.createDecoLocalFieldFrom(field));
      }
    }
  }

  private boolean isFieldUnassigned(String fieldName) {
    for (int i = 0, n = FIELD_REGION.length; i < n; i++) {
      String running = FIELD_REGION[i];
      if (running.contains("/**") || running.contains("//") || running.contains("/***")) {
        //skip cmt block
//        for (i += 1; i < n && (!searchRange[i].contains("//") && !searchRange[i].contains("*/")); i++) {}
        for (i += 1; i < n && !StringUtils.containsAny(FIELD_REGION[i], "*/", "**/", "***/", "//", "\n", "\r"); i++) {}
        i++;
        continue;
      }
      if (running.contains(fieldName)) {
        if (false == StringUtils.stripDoubleEndedNonAlphaNumeric(running).equalsIgnoreCase(fieldName)) {
          continue;
        }
        if (running.contains(";")) {
          return true;
        }
        if (running.contains("=")) { //fishy check
          for (; i < n; i++) {
            running = FIELD_REGION[i];
//            if (running.contains("null") || running.contains("false") || running.contains("true")) {
//              return true;
//            }
            if (!StringUtils.isEmpty(running) && running.contains(";")) {
              return true;
            }
            if (StringUtils.containsAny(FIELD_REGION[i], "\r", "\n", ";")) {
              break;
            }
          }
          return false;
        }
        for (; i < n; i++) { //ok we'll take it from here then bail.
          running = FIELD_REGION[i];
          if (running.contains("=")) {
            for (; i < n; i++) {
              running = FIELD_REGION[i];
              if (!StringUtils.isEmpty(running) && running.contains(";")) {
                return true;
              }
              if (StringUtils.containsAny(FIELD_REGION[i], "\r", "\n", ";")) {
                break;
              }
            }
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
  private boolean isSerializableField(Field field) {
    int modVal = field.getModifiers();
    if ((Modifier.isFinal(modVal) && Modifier.isStatic(modVal)) || (Modifier.isTransient(modVal) || field.isAnnotationPresent(Deprecated.class)) || field.getType() == Comparator.class || field.getType() == Logger.class || (Modifier.isFinal(modVal) && !isFieldUnassigned(field.getName()))) {
      return false;
    }
    if ((isFieldUnassigned(field.getName())) || (!Modifier.isStatic(modVal) && !isFieldUnassigned(field.getName()))) {
      return true;
    } else if (!Modifier.isStatic(modVal)) {
      return false;
    }
    return false;
  }
}
