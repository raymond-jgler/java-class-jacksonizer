package com.aggregated;


import java.util.Objects;


public class BuildConstructorPhaseOutput extends ChainedOutput {
  private StringBuilder               completeModifiedContent;
  private BuildConstructorPhaseOutput toReturn;

  public BuildConstructorPhaseOutput(String completeModifiedContent) {

    this.completeModifiedContent = new StringBuilder(completeModifiedContent);
    toReturn                     = this;
  }

  @Override
  public boolean failVerify() {
    return StringUtils.isEmpty(this.completeModifiedContent.toString());
  }

  @Override
  public void reset() {
    if (StringUtils.isNotEmpty(this.completeModifiedContent.toString())) {
      this.completeModifiedContent.setLength(0);
    } else {
      this.completeModifiedContent = new StringBuilder();
    }
  }

  @Override
  public ChainedOutput getValues() {
    if (Objects.isNull(this.toReturn)) {
      return new BuildConstructorPhaseOutput(completeModifiedContent.toString());
    }
    return this.toReturn;
  }

  @Override
  public Object getRawValues() {
    return this.completeModifiedContent;
  }

  @Override
  public String toString() {
    return "BuildConstructorPhaseOutput{" +
            "completeModifiedContent='" + completeModifiedContent + '\'' +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BuildConstructorPhaseOutput)) return false;
    BuildConstructorPhaseOutput that = (BuildConstructorPhaseOutput) o;
    return Objects.equals(completeModifiedContent, that.completeModifiedContent) && Objects.equals(
            toReturn,
            that.toReturn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(completeModifiedContent, toReturn);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}














