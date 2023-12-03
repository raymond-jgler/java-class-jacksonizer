package com.aggregated;

import java.util.ArrayList;
import java.util.List;
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
  private List<String> fullImportStringList;
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
    if (StringArsenal.current().with(fieldName).isEmpty()) {
      return fieldName;
    }
    if (fieldName.charAt(0) == '.') {
      fieldName = fieldName.substring(1, fieldName.length());
    }
    return fieldName;
  }

  public void addImportString(String importString) {
    if (Objects.isNull(this.fullImportStringList)) {
      this.fullImportStringList = new ArrayList<>();
    }
    if (StringArsenal.isEmpty(importString) || this.fullImportStringList.contains(importString)) {
      return;
    }
    this.fullImportStringList.add(importString);
  }

  public List<String> getFullImportStringList() {
    return fullImportStringList;
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
    if (o == null || getClass() != o.getClass()) return false;
    DecorationLocalField that = (DecorationLocalField) o;
    return Objects.equals(fieldName, that.fieldName) && Objects.equals(genericTypeName, that.genericTypeName) && Objects.equals(typeFullName, that.typeFullName) && Objects.equals(typeShortName, that.typeShortName) && Objects.equals(isFinal, that.isFinal) && Objects.equals(fullImportStringList, that.fullImportStringList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fieldName, genericTypeName, typeFullName, typeShortName, isFinal, fullImportStringList);
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















