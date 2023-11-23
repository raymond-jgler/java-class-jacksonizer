package com.aggregated;

import org.apache.commons.collections4.MapUtils;

import java.util.*;

public class ValidateClassPhaseOutput extends ChainedOutput {

  private List<Boolean>            validatedResults;
  private Map<String, Boolean>     carriedResults;
  private ValidateClassPhaseOutput toReturn;

  public Map<String, Boolean> getCarriedResults() {
    if (MapUtils.isEmpty(this.carriedResults)) {
      return null;
    }
    return this.carriedResults;
  }
  public boolean getByKey(String key) {
    if (MapUtils.isEmpty(this.carriedResults) || !this.carriedResults.containsKey(key)) {
      return false;
    }
    return this.carriedResults.get(key);
  }

  public ValidateClassPhaseOutput getToReturn() {
    return toReturn;
  }

  public ValidateClassPhaseOutput(Boolean... validatedResults) {
    this.validatedResults = Arrays.asList(validatedResults);
    this.carriedResults  = new HashMap<>();
    toReturn              = this;
  }

  private ValidateClassPhaseOutput(List<Boolean> results, Map<String, Boolean> carriedResults) {
    this.validatedResults  = results;
    this.carriedResults = carriedResults;
  }

  public void addCarried(String key, Boolean value) {
    if (Objects.isNull(this.carriedResults)) {
      this.carriedResults = new HashMap<>();
    }
    this.carriedResults.put(key, value);
  }

  public List<Boolean> getValidatedResults() {
    return validatedResults;
  }

  private ValidateClassPhaseOutput(List<Boolean> validatedResults) {
    this.validatedResults = validatedResults;
  }

  @Override
  public boolean failVerify() {
    for (Boolean res : validatedResults) {
      if (Boolean.FALSE == res.booleanValue()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void reset() {
    this.validatedResults = new ArrayList();
  }

  @Override
  public ChainedOutput getValues() {
    if (Objects.isNull(this.toReturn)) {
      return new ValidateClassPhaseOutput(validatedResults);
    }
    return this.toReturn;
  }

  @Override
  public Object getRawValues() {
    return validatedResults;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ValidateClassPhaseOutput)) return false;
    ValidateClassPhaseOutput that = (ValidateClassPhaseOutput) o;
    return Objects.equals(getValidatedResults(), that.getValidatedResults()) && Objects.equals(
            toReturn,
            that.toReturn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValidatedResults());
  }
  private ValidateClassPhaseOutput deepClone(ValidateClassPhaseOutput that) {
    return new ValidateClassPhaseOutput(that.validatedResults, that.carriedResults);
  }
  private ValidateClassPhaseOutput deepClone() {
    return deepClone(this);
  }
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return deepClone();
  }
}














