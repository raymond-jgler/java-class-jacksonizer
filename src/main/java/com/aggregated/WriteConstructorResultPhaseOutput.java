package com.aggregated;

import java.util.Objects;

public class WriteConstructorResultPhaseOutput extends ChainedOutput {
  private WriteConstructorResultPhaseOutput toReturn;

  public WriteConstructorResultPhaseOutput() {
  }

  @Override
  public ChainedOutput getValues() {
    if (Objects.isNull(this.toReturn)) {
      return new WriteConstructorResultPhaseOutput();
    }
    return this.toReturn;
  }

  @Override
  public Object getRawValues() {
    return null;
  }

  @Override
  public boolean failVerify() {
    return false;
  }

  @Override
  public void reset() {
    //do nothing
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof WriteConstructorResultPhaseOutput)) return false;
    WriteConstructorResultPhaseOutput that = (WriteConstructorResultPhaseOutput) o;
    return Objects.equals(toReturn, that.toReturn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(toReturn);
  }
}














