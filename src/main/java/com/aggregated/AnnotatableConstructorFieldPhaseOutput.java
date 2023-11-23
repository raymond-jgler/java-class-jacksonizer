package com.aggregated;

import java.util.Objects;

public class AnnotatableConstructorFieldPhaseOutput extends ChainedOutput {
  private CustomSerializationCollectedField collectedField;
  private AnnotatableConstructorFieldPhaseOutput toReturn;

  public AnnotatableConstructorFieldPhaseOutput(CustomSerializationCollectedField collectedField) {
    this.collectedField = collectedField;
    toReturn                = this;
  }

  @Override
  public boolean failVerify() {
    return false;
  }

  @Override
  public ChainedOutput getValues() {
    if (Objects.isNull(this.toReturn)) {
      this.toReturn = new AnnotatableConstructorFieldPhaseOutput(this.collectedField);
    }
    return this.toReturn;
  }

  @Override
  public Object getRawValues() {
    return this.collectedField;
  }

  @Override
  public void reset() {
    this.collectedField.reset();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AnnotatableConstructorFieldPhaseOutput)) return false;
    AnnotatableConstructorFieldPhaseOutput that = (AnnotatableConstructorFieldPhaseOutput) o;
    return Objects.equals(collectedField, that.collectedField) && Objects.equals(toReturn, that.toReturn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(collectedField, toReturn);
  }

  private AnnotatableConstructorFieldPhaseOutput deepClone(AnnotatableConstructorFieldPhaseOutput that) {
    CustomSerializationCollectedField cloned = that.collectedField.deepClone();
    return new AnnotatableConstructorFieldPhaseOutput(cloned);
  }
  private AnnotatableConstructorFieldPhaseOutput deepClone() {
    return deepClone(this);
  }
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return deepClone();
  }
}
