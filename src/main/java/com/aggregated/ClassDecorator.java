package com.aggregated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.util.*;

public class ClassDecorator {
  private static final Logger                                     LOG           =
          LoggerFactory.getLogger(ClassDecorator.class);
  private static final String                                     CLASS_KEYWORD =
          "org.example.JavaClassDecorator.class_decorator.ClassDecorator";
  private              InputReceiver                              inputReceiver;
  private              List<DecorationAlgorithm>                  algorithms;
  private              Map<DecorationStrategyMode, ExecutionRule> modeToRules;
  private              RawClientRuleInput                         ruleClientRawInput;

  private ClassDecorator() {
    reset();
  }

  public ClassDecorator addSuperConstructor(Boolean value) {
    this.ruleClientRawInput.addSuperConstructor(value);
    return this;
  }

  public ClassDecorator processDomainFieldBFS(Boolean val) {
    this.ruleClientRawInput.processDomainFieldBFS(val);
    return this;
  }

  public ClassDecorator processDomainImportBFS(Boolean value) {
    this.ruleClientRawInput.processDomainImportBFS(value);
    return this;
  }

  public ClassDecorator processDomainParamBFS(Boolean value) {
    this.ruleClientRawInput.processDomainParamBFS(value);
    return this;
  }

  public ClassDecorator processTopDownHierarchicalClasses(Boolean val) {
    this.ruleClientRawInput.processTopDownHierarchicalClasses(val);
    return this;
  }

  public void reset() {
    this.algorithms         = new ArrayList<>();
    this.modeToRules        = new HashMap<>();
    if (Objects.isNull(this.ruleClientRawInput)) {
      this.ruleClientRawInput = RawClientRuleInput.emptyInstance();
    }
    this.ruleClientRawInput.reset();
  }

  public ClassDecorator skipError(boolean skipError) {
    this.ruleClientRawInput.shouldSkipError(skipError);
    return this;
  }

  public ClassDecorator stripFinalClass(boolean isStripFinalClass) {
    this.ruleClientRawInput.stripFinalClass(isStripFinalClass);
    return this;
  }

  public ClassDecorator skipWhenBaseClass(Class... baseClass) {
    this.ruleClientRawInput.skipWhenBaseClass(Arrays.asList(baseClass));
    return this;
  }

  /**
   * First time sets constructor's annotation's package path.
   * 2nd time will do for field's.
   * @param annotationPackage
   * @return
   */
  public ClassDecorator addAnnotationPackage(String annotationPackage) {
    this.ruleClientRawInput.addAnnotationPackage(annotationPackage);
    return this;
  }

  public ClassDecorator withBaseClass(Class ...serializableClass) {
    this.ruleClientRawInput.withBaseClass(Arrays.asList(serializableClass));
    return this;
  }

  public static ClassDecorator emptyInstance() {
    return ClassDecoratorSingletonHolder.INSTANCE;
  }

  public ClassDecorator withInputType(InputReceiver inputReceiver, Object... inputValues) {
    NullabilityUtils.requireAllNonNull(true, inputValues);
    this.inputReceiver = InputReceiverFactory.fromValues(inputReceiver, inputValues);
    return this;
  }

  public ClassDecorator execSingleJava(String singleJava) {
    this.ruleClientRawInput.execSingleJavaFrom(singleJava);
    return this;
  }

  public ClassDecorator annotateConstructorWith(String ctorAnnotation) {
    this.ruleClientRawInput.annotateConstructorWith(ctorAnnotation);
    return this;
  }
  public ClassDecorator annotateParamsWith(String fieldAnnotation) {
    this.ruleClientRawInput.annotateParamsWith(fieldAnnotation);
    return this;
  }
  public ClassDecorator addCustomSerialization(String customSerKey, String fullCustomClassName, String fullAnnotName, String fullTypeName, RawClientRuleInput.SerializationMap.RULE_SCOPE... scopes) {
    this.ruleClientRawInput.buildSerialization(customSerKey, fullCustomClassName, fullAnnotName, fullTypeName, scopes);
    return this;
  }
  public ClassDecorator isAnyBaseClass(boolean isAnyBaseClass) {
    this.ruleClientRawInput.isAnyBaseClass(isAnyBaseClass);
    return this;
  }

  public ClassDecorator skipClassContainOrEndWith(String... args) {
    this.ruleClientRawInput.withIgnoredClasses(Arrays.asList(args));
    return this;
  }

  public ClassDecorator withSubPackages(boolean isRecursivePackage) {
    this.ruleClientRawInput.withSubPackages(isRecursivePackage);
    return this;
  }

  public ClassDecorator finalizeWithJacksonLib() {
    this.skipAnnotatedWith(JsonDeserialize.class, JsonSerialize.class)
            .skipWhenBaseClass(InvocationHandler.class,
                    Exception.class,
                    RuntimeException.class)
            .withBaseClass(Serializable.class)
            .withAccessModifier("private") //if write new, which access mod ?
            .annotateConstructorWith("JsonCreator")
            .annotateParamsWith("JsonProperty")
            .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package ctor's anntation
            .addAnnotationPackage("com.fasterxml.jackson.annotation") //-> package field
            .stripFinalClass(false)
            /**
             * Besides, constructor-based annotations,
             * this will annotate the declared fields in the class content.
             */
            .addCustomSerialization(RawClientRuleInput.CUSTOM_SERIALIZER, "com.domain.ExampleSerializer", "com.fasterxml.jackson.databind.annotation.JsonSerialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"))
            .addCustomSerialization(RawClientRuleInput.CUSTOM_DESERIALIZER, "com.domain.ExampleDeserializer", "com.fasterxml.jackson.databind.annotation.JsonDeserialize", "java.util.Date", RawClientRuleInput.CONSTRUCTOR_BODY.addContain("instanceof SampleLocalClass"));

    return this;
  }

  public ClassDecorator withStrategyModeAndRules(DecorationStrategyMode mode, ExecutionRule rules) {
    if (!this.modeToRules.isEmpty() || this.modeToRules.containsKey(mode)) {
      LOG.warn("Mode = " + modeToRules.get(mode) + " is already added, non-duplicate values are execpted ");
      return this;
    }
    this.modeToRules.putIfAbsent(mode, rules);
    return this;
  }

  public void execute() {
    /**
     * Generate and execute algorithms.
     */
    final long start = System.currentTimeMillis();
    generateAlgorithms();
    executeAlgorithms();
    final long end = System.currentTimeMillis();
//        LOG.info("\n\nFinished processing  " +  + " files");
    LOG.info("\n in " + (end - start) + " ms\n");
  }

  private void executeAlgorithms() {
    if (Objects.isNull(this.algorithms)) {
      throw new RuntimeException("Algorithms are null");
    }
    for (DecorationAlgorithm algorithm : algorithms) {
      try {
        algorithm.execute();
      } catch (Throwable throwable) {
        throw new RuntimeException("rip");
      }
    }
  }

  private void generateAlgorithms() {
    if (Objects.isNull(this.modeToRules) || MapUtils.isEmpty(this.modeToRules)) {
      if (StringUtils.isNotEmpty(this.ruleClientRawInput.getSingleJavaFileName())) {
        this.algorithms  = new ArrayList<>();
        this.algorithms.add(DecorationAlgorithmFactory.fromStrategyMode(InputReceiver.STRING_BASED_INPUT,
                DecorationStrategyMode.ANNOTATE_PARAMERTISED_CONSTRUCTOR,
                ExecutionRule.ALL,
                this.ruleClientRawInput));
        return;
      }
      throw new RuntimeException("Strategy mode is unknown ");
    }
    this.algorithms =
            DecorationAlgorithmFactory.fromStrategyModes(this.modeToRules, this.inputReceiver, this.ruleClientRawInput);
  }
  public ClassDecorator skipAnnotatedWith(Class... annotationNames) {
    this.ruleClientRawInput.skipAnnotatedWith(Arrays.asList(annotationNames));
    return this;
  }

  public ClassDecorator withAccessModifier(String accessModifier) {
    this.ruleClientRawInput.withAccessModifier(accessModifier);
    return this;
  }

  private static class ClassDecoratorSingletonHolder {
    private static final ClassDecorator INSTANCE = getInstance();

    private static ClassDecorator getInstance() {
      if (Objects.nonNull(INSTANCE)) {
        return INSTANCE;
      }
      return new ClassDecorator();
    }
  }
}














