package com.aggregated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ValidateClassPhase extends BaseConstructorPhaseAlgorithm {
  private ValidateClassPhase() {
    super(null);
  }
  private static final String VALIDATE_CLASS_PHASE = "VALIDATE_CLASS_PHASE";

  public ValidateClassPhase(RawClientRuleInput rawInput) {
    super(rawInput);
  }

  public static String getCode() {
    return VALIDATE_CLASS_PHASE;
  }

  @Override
  public PhaseChainedResult execute(PhaseChainedResult previousInput) {
    ValidateClassPhaseOutput result = new ValidateClassPhaseOutput(!hasOnlySerialUID(),
            !isSkipWithBaseClass(),
            !isForbidden(),
            !isSkip(),
            !skipAnnotated());

    result.addCarried(SHOULD_ADD_DEFAULT_CTOR, shouldAddDefaultCtor());
    try {
      pushStack((PhaseChainedResult) result.clone());
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  /**
   * A bit fishy, this is associative with
   * its caller, b/c merely relying on reflection won't
   * exactly locate default ctor's string representation.
   * @return
   */
  private boolean shouldAddDefaultCtor() {
    return ReflectionUtils.hasMutator(CLAZZ);
  }

  /**
   * //TODO If class is fully commented ?
   *
   * @return
   */
  private boolean isBlockedByCodeComments() {
    return false;
  }

  private boolean hasGoodConstructor() {
    if (isSkip() || CLAZZ.isAnnotationPresent(JsonDeserialize.class) || CLAZZ.isAnnotationPresent(JsonSerialize.class)) {
      return false;
    }
    List<Constructor> declaredCtors = Arrays.asList(CLAZZ.getDeclaredConstructors());

    Boolean isJacksonAnnotated = Boolean.parseBoolean(null);
    for (Constructor ctor : declaredCtors) {
      if (ctor.getParameterCount() >= 0 ) {
        if (ctor.isAnnotationPresent(JsonCreator.class)) {
          isJacksonAnnotated = true;
          break;
        }
      }
    }
    /**
     * all conditions must be true
     */
    return allBoolsResolvedTo(Optional.of(Boolean.FALSE), false, isJacksonAnnotated);
  }

  public static boolean hardCodeIsGoodClass(Class clazz) {
    if (clazz.isAnnotationPresent(JsonDeserialize.class) || clazz.isAnnotationPresent(JsonSerialize.class)
            || StringArsenal.current().containsAny(clazz.getSimpleName(), "serializer", "deserializer") || clazz.getSimpleName().equalsIgnoreCase("Builder")) {
      return false;
    }
    Boolean isJacksonAnnotated = Boolean.parseBoolean(null);
    for (Constructor ctor : clazz.getDeclaredConstructors()) {
      if (ctor.getParameterCount() >= 0 ) {
        if (ctor.isAnnotationPresent(JsonCreator.class)) {
          isJacksonAnnotated = true;
          break;
        }
      }
    }
    return !isJacksonAnnotated;
  }

  /**
   * All args must be resolved to
   * given boolean value
   * @param finalValue defaulted to TRUE
   * @return
   */
  private boolean allBoolsResolvedTo(Optional<Boolean> finalValue, boolean...args) {
    if (!finalValue.isPresent()) {
      finalValue = Optional.of(Boolean.TRUE);
    }
    for (boolean each : args) {
      if (finalValue.get() != each) {
        return false;
      }
    }
    return true;
  }
  private boolean isSkipWithBaseClass() {
    if (CollectionUtils.isEmpty(rawInput.getSkipBaseClasses())) {
      return false;
    }
    for (Class clazz : rawInput.getSkipBaseClasses()) {
      if (clazz.isAssignableFrom(CLAZZ)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if
   * having only field = private static final long serialVersionUID = 1L;
   * and no paramertized constructor.
   */
  private boolean hasOnlySerialUID() {
    Field[] fields = CLAZZ.getDeclaredFields();
    return !hasGoodConstructor() && fields.length == 1 && Modifier.isStatic(fields[0].getModifiers()) && Modifier.isFinal(fields[0].getModifiers()) && fields[0].getType() == long.class;
  }

  private boolean isSkip() {
    /**
     * We only proceed
     * if this class is derived from 1 of these given base classes.
     */
    if (!isSubClassOf(CLAZZ)) {
      return false;
    }
    if (!CLAZZ.getName().contains("$") && StringArsenal.current().containsAny(CLASS_CONTENT, rawInput.getCtorAnnotation(), rawInput.getFieldAnnotation())) {
      return true;
    }
    final String       className      = CLAZZ.getSimpleName();
    final List<String> skippedClasses = rawInput.getSkipClasses();
    /**
     * Don't skip
     * if there are inner classes
     */
    if (Objects.isNull(skippedClasses) || CLAZZ.getDeclaredClasses().length > 0) {
      return false;
    }
    for (String eachSkipped : skippedClasses) {
      if (StringArsenal.current().containsAny(className, eachSkipped) || StringArsenal.current().endsWithAny(className,
              eachSkipped) || className.equalsIgnoreCase(eachSkipped)
              || eachSkipped.equalsIgnoreCase(className)) {
        return true;
      }
    }
    return false;
  }
  private boolean isSubClassOf(Class clazz) {
    if (CollectionUtils.isEmpty(rawInput.getWithBaseClasses())) {
      return true;
    }
    for (Class eachBaseClass : rawInput.getWithBaseClasses()) {
      if (clazz.isAssignableFrom(eachBaseClass)) {
        return true;
      }
    }
    return false;
  }
  private boolean isForbidden() {
    return ReflectionUtils.isForbidden(CLAZZ, rawInput);
  }

  private boolean skipAnnotated() {
    //TODO later...don't remember what it is
    if (Objects.isNull(CLAZZ)) {
      return false;
    }
    for (Class annotation : rawInput.getAnnotationClasses()) {
      if (CLAZZ.isAnnotationPresent(annotation)) {
        return true;
      }
    }
    return false;
  }

}














