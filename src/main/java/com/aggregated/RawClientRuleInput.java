package com.aggregated;

import java.util.*;

public class RawClientRuleInput {
  private List<String> skipClasses;
  private boolean isSubPackages;
  private boolean isAnyBaseClass;
  private List<Class> annotationClasses;
  private String accessModifier;
  private List<Class> skipBaseClasses;
  private boolean skipError;
  private String ctorAnnotation;
  private String fieldAnnotation;
  private String ctorAnnotationPackage;
  private String fieldAnnotationPackage;
  private String singleJavaFileName;
  private boolean isStripFinalClass;
  private boolean isCustomSerialization;
  public final static String CUSTOM_SERIALIZER = "CUSTOM_SER";
  public final static String CUSTOM_DESERIALIZER = "CUSTOM_DESER";
  public final static SerializationMap.RULE_SCOPE DECLARED_FIELDS = SerializationMap.RULE_SCOPE.DECLARED_FIELDS;
  public final static SerializationMap.RULE_SCOPE CONSTRUCTOR_BODY = SerializationMap.RULE_SCOPE.CONSTRUCTOR_BODY;
  private Map<String, SerializationMap> customSerMap;
  private List<Class>                   withBaseClasses;
  private Boolean bfsImports = false;
  private Boolean bfsFields = false;
  private Boolean withClassHierarchy = false;
  private Boolean bfsParams = false;
  private Boolean withSuperConstructor = false;

  public RawClientRuleInput addSuperConstructor(Boolean value) {
    this.withSuperConstructor = value;
    return this;
  }

  public List<Class> getWithBaseClasses() {
    return withBaseClasses;
  }

  public RawClientRuleInput processDomainFieldBFS(Boolean val) {
    this.bfsFields = val;
    return this;
  }

  public Boolean getWithSuperConstructor() {
    return withSuperConstructor;
  }

  public Boolean getBfsParams() {
    return bfsParams;
  }

  public Boolean getWithClassHierarchy() {
    return withClassHierarchy;
  }

  public Boolean getBfsFields() {
    return bfsFields;
  }

  public RawClientRuleInput processDomainImportBFS(Boolean value) {
    this.bfsImports = value;
    return this;
  }

  public Boolean getBfsImports() {
    return bfsImports;
  }

  public RawClientRuleInput processDomainParamBFS(Boolean value) {
    this.bfsParams = value;
    return this;
  }

  public RawClientRuleInput processTopDownHierarchicalClasses(Boolean val) {
    this.withClassHierarchy = val;
    return this;
  }

  public void setWithBaseClasses(List<Class> withBaseClasses) {
    this.withBaseClasses = withBaseClasses;
  }

  public boolean isStripFinalClass() {
    return isStripFinalClass;
  }

  public List<String> getImportStrings() {
    if (Objects.isNull(this.customSerMap)) {
      return null;
    }
    SerializationMap serMap = this.customSerMap.get(CUSTOM_SERIALIZER);
    SerializationMap deserMap = this.customSerMap.get(CUSTOM_DESERIALIZER);
    if (!NullabilityUtils.requireAllNonNull(false, serMap, deserMap)) {
      return null;
    }
    if (StringUtils.isEmpty(serMap.getValues( " ")) || StringUtils.isEmpty(deserMap.getValues(" "))) {
      return null;
    }
    return Arrays.asList(serMap.getValues(" "), deserMap.getValues(" "));
  }

  public Map<String, String> getFilterRules() {
    if (Objects.isNull(this.customSerMap)) {
      return null;
    }
    SerializationMap serMap = this.customSerMap.get(CUSTOM_SERIALIZER);
    SerializationMap deserMap = this.customSerMap.get(CUSTOM_DESERIALIZER);
    if (!NullabilityUtils.requireAllNonNull(false, serMap, deserMap)) {
      return null;
    }
    if (StringUtils.isEmpty(serMap.getValues( " ")) || StringUtils.isEmpty(deserMap.getValues(" "))) {
      return null;
    }
    Map<String, String> result = new HashMap<>();
    for (SerializationMap.FilterRules filterRule : serMap.getFilterRules()) {
      result.put(filterRule.getScope().getStringForm(), filterRule.generateRule(true));
    }
    return result;
  }

  public void setStripFinalClass(boolean stripFinalClass) {
    isStripFinalClass = stripFinalClass;
  }

  private RawClientRuleInput() {
    reset();
  }

  public String getSingleJavaFileName() {
    return this.singleJavaFileName;
  }

  public List<Class> getSkipBaseClasses() {
    return skipBaseClasses;
  }

  public void reset() {
    skipClasses = new ArrayList<>();
    annotationClasses = new ArrayList<>();
    isSubPackages = false;
    isAnyBaseClass = false;
    accessModifier = "";
    customSerMap = new HashMap<>();
    singleJavaFileName = "";
    bfsFields = false;
    bfsImports = false;
    bfsParams = false;
    withSuperConstructor = false;
    withClassHierarchy = false;
  }

  public RawClientRuleInput withAccessModifier(String value) {
    this.accessModifier = value;
    return this;
  }

  public RawClientRuleInput execSingleJavaFrom(String singleJavaFileName) {
    this.singleJavaFileName = singleJavaFileName;
    return this;
  }

  public RawClientRuleInput annotateConstructorWith(String ctorAnnotation) {
    this.ctorAnnotation = ctorAnnotation;
    return this;
  }

  public RawClientRuleInput annotateParamsWith(String fieldAnnotation) {
    this.fieldAnnotation = fieldAnnotation;
    return this;
  }

  public RawClientRuleInput addAnnotationPackage(String packageName) {
    if (StringUtils.isNotEmpty(this.ctorAnnotationPackage) && StringUtils.isNotEmpty(this.fieldAnnotationPackage)) {
      return this;
    }
    /**
     * First time is of constructor's annotation.
     * 2nd time is for field.
     * Dunno if should split into separate methods.
     */
    if (StringUtils.isEmpty(this.ctorAnnotationPackage)) {
      this.ctorAnnotationPackage = packageName;
      return this;
    }
    this.fieldAnnotationPackage = packageName;
    return this;
  }

  public String getCtorAnnotation() {
    return ctorAnnotation;
  }

  public String getFieldAnnotation() {
    return fieldAnnotation;
  }

  public RawClientRuleInput shouldSkipError(boolean skipError) {
    this.skipError = skipError;
    return this;
  }

  public String getCtorAnnotationPackage() {
    return ctorAnnotationPackage;
  }

  public String getFieldAnnotationPackage() {
    return fieldAnnotationPackage;
  }

  public boolean isSkipError() {
    return skipError;
  }

  public RawClientRuleInput skipAnnotatedWith(List<Class> annotationClasses) {
    this.annotationClasses = annotationClasses;
    return this;
  }

  //TODO, work-around-able
  public RawClientRuleInput isAnyBaseClass(boolean isAnyBaseClass) {
    if (isAnyBaseClass) {
      this.isAnyBaseClass = isAnyBaseClass;
    }
    return this;
  }

  public String getAccessModifier() {
    return accessModifier;
  }

  public void skipWhenBaseClass(List<Class> baseClass) {
    this.skipBaseClasses = baseClass;
  }

  public RawClientRuleInput withBaseClass(List<Class> baseClasses) {
    this.withBaseClasses = baseClasses;
    return this;
  }

  public RawClientRuleInput withIgnoredClasses(List<String> skipClasses) {
    if (Objects.isNull(skipClasses)) {
      throw new IllegalArgumentException("Invalid input");
    }
    this.skipClasses = skipClasses;
    return this;
  }

  public RawClientRuleInput withSubPackages(boolean isSubPackages) {
    if (isSubPackages) {
      this.isSubPackages = isSubPackages;
    }
    return this;
  }

  //TODO, work-around-able
  public boolean isAnyBaseClass() {
    return isAnyBaseClass;
  }

  public List<Class> getAnnotationClasses() {
    return annotationClasses;
  }

  public List<String> getSkipClasses() {
    return skipClasses;
  }

  public boolean isSubPackages() {
    return isSubPackages;
  }

  public static RawClientRuleInput emptyInstance() {
    return RuleClientRawInputSingletonHolder.INSTANCE;
  }

  public RawClientRuleInput stripFinalClass(boolean isStripFinalClass) {
    this.isStripFinalClass = isStripFinalClass;
    return this;
  }

  public RawClientRuleInput buildSerialization(String customSerKey, String fullCustomClassName, String fullAnnotName, String fullTypeName, SerializationMap.RULE_SCOPE... ruleScope) {
    NullabilityUtils.isAllNonEmpty(true, customSerKey, fullCustomClassName, fullAnnotName, fullTypeName, String.valueOf(ruleScope));
    SerializationMap serializationMap = new SerializationMap(fullCustomClassName, fullAnnotName, fullTypeName, ruleScope);
    if (Objects.isNull(this.customSerMap)) {
      this.customSerMap = new HashMap<>();
    }
    this.isCustomSerialization = true;
    this.customSerMap.put(customSerKey, serializationMap);
    return this;
  }

  public String getCustomSerRequiredFieldType() {
    if (Objects.isNull(this.customSerMap)) {
      return null;
    }
    return this.customSerMap.get(CUSTOM_SERIALIZER).getFullTypeName();
  }

  public boolean isCustomSerialization() {
    return isCustomSerialization;
  }

  public void setCustomSerialization(boolean customSerialization) {
    isCustomSerialization = customSerialization;
  }

  public static class SerializationMap {
    private final String fullCustomClassName;
    private final String fullAnnotationName;
    private final String fullTypeName;
    private final List<RULE_SCOPE> ruleScope;
    private static List<FilterRules> filterRules;

    private SerializationMap(String fullCustomClassName, String fullAnnotationName, String fullTypeName, RULE_SCOPE... ruleScope) {
      this.fullCustomClassName = fullCustomClassName;
      this.fullAnnotationName = fullAnnotationName;
      this.fullTypeName = fullTypeName;
      this.ruleScope = Arrays.asList(ruleScope);
      makeRules();
    }
    public List<RULE_SCOPE> getRuleScope() {
      return ruleScope;
    }

    public List<FilterRules> getFilterRules() {
      return filterRules;
    }

    private void makeRules() {
      if (Objects.isNull(this.filterRules)) {
        this.filterRules = new ArrayList<>();
      }
      for (RULE_SCOPE scope : this.ruleScope) {
        FilterRules rule = new FilterRules(fullTypeName, scope, scope.getContain());
        if (filterRules.contains(rule)){
          continue;
        }
        filterRules.add(rule);
      }
    }
    public String getValues(String separator) {
      return fullCustomClassName + separator + fullAnnotationName;
    }
    public String getFullCustomClassName() {
      return fullCustomClassName;
    }

    public String getFullAnnotationName() {
      return fullAnnotationName;
    }

    public String getFullTypeName() {
      return fullTypeName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SerializationMap)) return false;
      SerializationMap that = (SerializationMap) o;
      return Objects.equals(getFullCustomClassName(), that.getFullCustomClassName()) && Objects.equals(getFullAnnotationName(), that.getFullAnnotationName()) && Objects.equals(getFullTypeName(), that.getFullTypeName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getFullCustomClassName(), getFullAnnotationName(), getFullTypeName());
    }

    @Override
    public String toString() {
      return "SerializationMap{" +
              "fullCustomClassName='" + fullCustomClassName + '\'' +
              ", fullAnnotationName='" + fullAnnotationName + '\'' +
              ", fullTypeName='" + fullTypeName + '\'' +
              '}';
    }

    public static class RULE_SCOPE {
      public static RULE_SCOPE DECLARED_FIELDS = emptyInstance();
      public static RULE_SCOPE CONSTRUCTOR_BODY = emptyInstance();
      private String contain;

      private static RULE_SCOPE emptyInstance() {
        return new RULE_SCOPE();
      }

      public String getStringForm() {
        if (this.equals(CONSTRUCTOR_BODY)) {
          return "CONSTRUCTOR_BODY";
        }
        return "DECLARED_FIELDS";
      }

      private RULE_SCOPE() {}
      RULE_SCOPE(String contain) {
        contain = contain;
      }
      public RULE_SCOPE addContain(String CONTAIN) {
        contain = CONTAIN;
        return this;
      }
      public String getContain() {
        return contain;
      }

      @Override
      public String toString() {
        return "RULE_SCOPE{" + "key=" + getStringForm() +
                "contain='" + contain + '\'' +
                '}';
      }
    }

    private static class FilterRules {
      private final String fullTypeName;
      private final RULE_SCOPE scope;
      private static final String CONTAIN_CONNECTIVE = "CONTAINS";
      private final String containsStrings;

      public FilterRules(String fullTypeName, RULE_SCOPE scope, String containsStrings) {
        this.fullTypeName = fullTypeName;
        this.scope = scope;
        this.containsStrings = containsStrings;
      }
      public String getFullTypeName() {
        return fullTypeName;
      }
      public String getContainsStrings() {
        return containsStrings;
      }

      public RULE_SCOPE getScope() {
        return scope;
      }

      public String getContainsWord() {
        return containsStrings;
      }
      public String generateRule(boolean isPartial) {
        StringBuilder generated = new StringBuilder();
        generated
                .append(isPartial ? "" :  this.scope.getStringForm() + ",")
                .append(CONTAIN_CONNECTIVE)
                .append(",")
                .append(containsStrings);
        return generated.toString();
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterRules)) return false;
        FilterRules that = (FilterRules) o;
        return Objects.equals(getFullTypeName(), that.getFullTypeName()) && Objects.equals(getScope(), that.getScope()) && Objects.equals(getContainsStrings(), that.getContainsStrings());
      }

      @Override
      public int hashCode() {
        return Objects.hash(getFullTypeName(), getScope(), getContainsStrings());
      }
    }
  }

  private static class RuleClientRawInputSingletonHolder {
    private static final RawClientRuleInput INSTANCE = getInstance();

    private static final RawClientRuleInput getInstance() {
      if (Objects.isNull(INSTANCE)) {
        return new RawClientRuleInput();
      }
      return INSTANCE;
    }
  }
}














