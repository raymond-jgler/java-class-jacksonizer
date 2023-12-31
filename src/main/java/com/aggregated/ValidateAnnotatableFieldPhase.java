package com.aggregated;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.Objects;
import org.slf4j.Logger;

import static com.aggregated.CharacterRepository.*;

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
    AnnotatableConstructorFieldPhaseOutput result =
            new AnnotatableConstructorFieldPhaseOutput(collectedField);
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

  private boolean isFieldAssigned(String fieldName) {
    for (int i = 0, n = FIELD_REGION.length; i < n; i++) {
      String running = FIELD_REGION[i];
      // TODO
      //      if (running.contains("/**") || running.contains("/***")) {
      //        continue;
      //      }
      if (running.contains(fieldName)) {
        if (running.contains(RAW_EQUAL)) {
          return true;
        }
        return false;
      }
    }
    return false;
  }

  private boolean isSerializableField(Field field) {
    int modVal = field.getModifiers();
    if (Modifier.isFinal(modVal) && Modifier.isStatic(modVal)) {
      return false;
    }
    final boolean isFieldAssignedFlag = isFieldAssigned(field.getName());
    if (Modifier.isFinal(modVal)
            && (isFieldAssignedFlag
            || (Modifier.isTransient(modVal) || field.isAnnotationPresent(Deprecated.class))
            || field.getType() == Comparator.class
            || field.getType() == Logger.class)) {
      return false;
    }
    if (isFieldAssignedFlag || (!Modifier.isStatic(modVal) && !isFieldAssignedFlag)) {
      return true;
    }
    return false;
  }
}
