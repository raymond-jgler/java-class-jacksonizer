













package com.aggregated;

import java.util.Objects;

/**
 * A more-focused version of java.reflections.Field
 */
public class DecorationLocalField {
  private final String fieldName;
  private final String genericTypeName;
  private final String typeFullName;
  private final String typeShortName;
  private final Boolean isFinal;
  private DecorationLocalField(String fieldName, String genericTypeName, String typeFullName, String typeShortName, Boolean isFinal) {
    this.fieldName       = fieldName;
    this.genericTypeName = genericTypeName;
    this.typeFullName    = typeFullName;
    this.typeShortName   = typeShortName;
    this.isFinal = Objects.isNull(isFinal) ? false : isFinal;
  }

  public static DecorationLocalField createFrom(String fieldName, String genericTypeName, String typeFullName, String typeShortName, Boolean isFinal) {
    NullabilityUtils.isAllNonEmpty(Boolean.FALSE, fieldName, genericTypeName, typeFullName, typeShortName);
    return new DecorationLocalField(fishyStripFirstDot(fieldName), fishyStripFirstDot(genericTypeName), fishyStripFirstDot(typeFullName), fishyStripFirstDot(typeShortName), isFinal);
  }

  private static String fishyStripFirstDot(String fieldName) {
    if (fieldName.charAt(0) == '.') {
      fieldName = fieldName.substring(1, fieldName.length());
    }
    return fieldName;
  }

  public Boolean getFinal() {
    return isFinal;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getGenericTypeName() {
    return genericTypeName;
  }

  public String getTypeFullName() {
    return typeFullName;
  }

  public String getTypeShortName() {
    return typeShortName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecorationLocalField)) return false;
    DecorationLocalField field = (DecorationLocalField) o;
    return Objects.equals(getFieldName(), field.getFieldName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getFieldName());
  }

  /**
   * Answers a string containing a concise, human-readable
   * description of the receiver.
   *
   * @return String a printable representation for the receiver.
   */
  @Override
  public String toString() {
    return super.toString();
  }
}

