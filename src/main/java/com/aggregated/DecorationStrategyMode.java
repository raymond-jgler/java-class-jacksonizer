













package com.aggregated;

public enum DecorationStrategyMode {
  DEFAULT,
  DEFAULT_CONSTRUCTOR,
  /**
   * Jackson,
   *
   * @JsonCreator above the ctor code
   * @JsonProperty for each field
   * Will skip classes having default ctor.
   */
  JACKSON_PARAMETERISED_CONSTRUCTOR,

  /**
   * Will annotate the existing parameterised constructor ( commonly the most "complete" one - containing all fields for
   * deserialization).
   *, and fields
   * with given respective annotations.
   */
  ANNOTATE_PARAMERTISED_CONSTRUCTOR,
  GETTER,
  SETTER;

  DecorationStrategyMode() {
  }

  @Override
  public String toString() {
    return super.toString();
  }
}

