package com.aggregated;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ReflectionUtils {
  private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtils.class);
  /**
   * forcefully get the class object from
   * string path name
   * @param unresolvedName must be single-dotted
   * @return
   */
  public static Class getClass(String unresolvedName) {
    if (StringArsenal.current().isAllLowerCase(unresolvedName)) {
      return null;
    }
    String toPrint = unresolvedName;
    Class<?> clazz = null;
    int dotIdx = -1;
    do {
      try {
        clazz = Class.forName(StringArsenal.current().stripDoubleEndedNonAlphaNumeric(unresolvedName));
      } catch (ClassNotFoundException e) {
        try {
          unresolvedName = StringArsenal.current().resolveReplaces(unresolvedName,
                  "//",
                  ".",
                  "\\",
                  ".",
                  "src.main.java",
                  "",
                  "**",
                  "",
                  "\\.+",
                  ".",
                  ".java",
                  "");
          clazz = Class.forName(unresolvedName);
        } catch (ClassNotFoundException ex) {
          dotIdx = unresolvedName.indexOf(".");
          if (dotIdx == -1) {
            break;
          }
          unresolvedName = unresolvedName.substring(dotIdx + 1, unresolvedName.length());
        }
      }
    } while (Objects.isNull(clazz) && unresolvedName != "");

    if (Objects.isNull(clazz)) {
//      LOG.error("failed to get class from " + toPrint);
    }
    return clazz;
  }

  public static boolean isMutator(Method method) {
    return (method.getName().startsWith("set") || method.getName()
            .startsWith("add")) && method.getParameterCount() >= 1;
  }

  /**
   * By default,
   * this will return the top-down hierarchical class list,
   * top parent -> so on
   * if no parent, class itself is returned by default.
   * the first class is the top-level parent.
   * @param clazz
   * @param isPreserveOriginalClass
   * @return
   */
  public static List<Class> buildTopDownClassesFrom(Class clazz, boolean isPreserveOriginalClass) {
    AtomicReference<List<Class>> topDownClassHierarchyList = new AtomicReference(new ArrayList<>());
    rcsBuildHierarchyClassList(clazz, topDownClassHierarchyList);
    List<Class> res = topDownClassHierarchyList.get();
    if (isPreserveOriginalClass) {
      return res;
    }
    if (res.size() == 1) {
      return res;
    }
    res.remove(res.size() - 1);
    return res;
  }

  /**
   * A head-based recursive method to find the top parent class,
   * down to the passed in class.
   * @param current
   * @return a list of hierarchical class list.
   */
  private static void rcsBuildHierarchyClassList(Class current, AtomicReference<List<Class>> listAtomicReference) {
    if (Objects.isNull(current) || Object.class.equals(current.getSuperclass())) {
      if (Objects.nonNull(current)) {
        listAtomicReference.get().add(current);
      }
      return;
    }
    rcsBuildHierarchyClassList(current.getSuperclass(), listAtomicReference);
    listAtomicReference.get().add(current);
  }

  public static boolean isForbidden(Class CLAZZ, RawClientRuleInput rawInput) {
    if (NullabilityUtils.isAnyNullIn(CLAZZ, rawInput)) {
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
    return CLAZZ.isInterface() || CLAZZ.isEnum() || CLAZZ.isAnnotation();
  }
  public static boolean hasMutator(Class<?> clazz) {
    Method[] methods = clazz.getDeclaredMethods();

    for (Method method : methods) {
      if (isMutator(method)) {
        return true;
      }
    }

    return false;
  }

  public static boolean hardCodeIsJackson(Class CLAZZ) {
    if (CLAZZ.isAnnotationPresent(JsonDeserialize.class) || CLAZZ.isAnnotationPresent(JsonSerialize.class) || CLAZZ.getSimpleName().equalsIgnoreCase("Builder")) {
      return true;
    }
    return false;
  }
  public static boolean hasInnerClasses(Class clazz) {
    if (Objects.isNull(clazz)) {
      return false;
    }
    return clazz.getDeclaredClasses().length > 0 || clazz.getClasses().length > 0;
  }

  private static Boolean isFieldUnassigned(String fieldName) {
    String[] FIELD_REGION = BaseConstructorPhaseAlgorithm.getFieldRegion();
    if (CollectionUtils.isEmpty(Arrays.asList(FIELD_REGION))) {
      LOG.warn("Data is not init-ed");
      return null;
    }
    for (int i = 0, n = FIELD_REGION.length; i < n; i++) {
      String running = FIELD_REGION[i];
      if (running.contains("/**") || running.contains("//") || running.contains("/***")) {
        //skip cmt block
//        for (i += 1; i < n && (!searchRange[i].contains("//") && !searchRange[i].contains("*/")); i++) {}
        for (i += 1; i < n && !StringArsenal.current().containsAny(FIELD_REGION[i], "*/", "**/", "***/", "//", "\n", "\r"); i++) {
        }
        i++;
        continue;
      }
      if (running.contains(fieldName)) {
        if (false == StringArsenal.current().resolveReplaces(running, "\r", "", "\n", "", " ", "").equals(fieldName)) {
          continue;
        }
        if (running.contains(";")) {
          return true;
        }
        if (running.contains("=")) { //fishy check
          for (; i < n; i++) {
            running = FIELD_REGION[i];
            if (running.contains("null") || running.contains("false") || running.contains("true")) {
              return true;
            }
            if (StringArsenal.current().containsAny(FIELD_REGION[i], "\r", "\n", ";")) {
              break;
            }
          }
          return false;
        }
        for (; i < n; i++) { //ok we'll take it from here then bail.
          running = FIELD_REGION[i];
          if (running.contains("=")) {
            for (; i < n; i++) {
              running = FIELD_REGION[i];
              if (running.contains("null") || running.contains("false") || running.contains("true")) {
                return true;
              }
              if (StringArsenal.current().containsAny(FIELD_REGION[i], "\r", "\n", ";")) {
                break;
              }
            }
            return false;
          }
          if (running.contains(";")) {
            break;
          }
        }
      }
    }
    return true;
  }
  private static boolean evalBoolean(Boolean obj) {
    return Objects.nonNull(obj) && obj.booleanValue();
  }

  public static List<DecorationLocalField> makeSerializableFieldsFrom(Class CLAZZ) {
    BaseConstructorPhaseAlgorithm.beginWith(CLAZZ);
    List<DecorationLocalField> built = new ArrayList<>();
    for (Field field : CLAZZ.getDeclaredFields()) {
      if (field.getName().contains("$this")) {
        continue;
      }
      if (ReflectionUtils.isSerializableField(field)) {
        built.add(createDecoLocalFieldFrom(field));
      }
    }
    return built;
  }

  public static DecorationLocalField createDecoLocalFieldFrom(Field field) {
    return DecorationLocalField.createFrom(field.getName(), field.getGenericType().getTypeName(), field.getType().getName(), field.getType().getSimpleName(), Modifier.isFinal(field.getModifiers()));
  }

  public static boolean isSerializableField(Field field) {
    int modVal = field.getModifiers();
    if ((Modifier.isFinal(modVal) && Modifier.isStatic(modVal)) || (Modifier.isTransient(modVal) || field.isAnnotationPresent(Deprecated.class)) || field.getType() == Comparator.class || field.getType() == Logger.class || (Modifier.isFinal(modVal) && !evalBoolean(isFieldUnassigned(field.getName())))) {
      return false;
    }
    if (!Modifier.isStatic(modVal) && evalBoolean(isFieldUnassigned(field.getName()))) {
      return true;
    } else if (!Modifier.isStatic(modVal)) {
      return false;
    }
    return false;
  }
  public static List<DecorationLocalField> merge(List<DecorationLocalField> from, List<DecorationLocalField> with) {
    if (CollectionUtils.isEmpty(with)) {
      return from;
    }
    if (CollectionUtils.isEmpty(from)) {
      return with;
    }
    List<DecorationLocalField> merged = new ArrayList<>(from);
    for (DecorationLocalField field : with) {
      if (merged.contains(field)) {
        continue;
      }
      merged.add(field);
    }
    return merged;
  }

}














