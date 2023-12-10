package com.aggregated;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class AnnotatableConstructorDecorator extends BaseDecorationAlgorithm {
  private static final Logger LOG = LoggerFactory.getLogger(AnnotatableConstructorDecorator.class);

  private static Set<String> visited = new HashSet<>();
  private static Queue<Class> lineUps = new LinkedList<>();

  public AnnotatableConstructorDecorator(InputReceiver inputReceiver, ExecutionRule rule,
                                         RawClientRuleInput rawInput,
                                         DecorationStrategyMode mode) {
    super(inputReceiver, rule, rawInput, mode);
  }

  @Override
  protected boolean isValidStrategy() {
    if (!DecorationStrategyMode.ANNOTATE_PARAMERTISED_CONSTRUCTOR.equals(mode)) {
      throw new RuntimeException("Strategy mode is invalid");
    }
    return true;
  }

  @Override
  protected void executePhases(PhaseChainedResult previousInput) {
    PhaseChainedResult runningPhaseResultOutput = InitialDummyPhase.emptyInstance();
    for (BaseConstructorPhaseAlgorithm phase : algorithmPhases) {
      try {
        runningPhaseResultOutput = phase.execute(previousInput);
      } catch (Throwable throwable) {
        LOG.warn(throwable.getMessage());
      }
      previousInput            = runningPhaseResultOutput;
      if (runningPhaseResultOutput.failVerify()) {
        LOG.info("Class is skipped at phase = " + runningPhaseResultOutput + "need by-hand verifications !");
        break;
      }
    }
  }
  public static void enqueueWith(Class clz) {
    if (Objects.isNull(clz) || lineUps.contains(clz) || visited.contains(cleanseClassPath(clz.getName()))
            || StringUtils.containsAny(clz.getName(), "org", "java", "java.util")) {
      return;
    }
    lineUps.offer(clz);
  }

  /**
   *
   * By default,
   * this will use BFS to explore and process all domain-related superFields.
   * if flag is set to true
   *  then BFS to explore all imports as well - CAUTION : IT WILL PROCESS MANY CLASSES.
   * @return
   */
  @Override
  public ExecutionResult execute() {
    if (!isValidStrategy()) {
      return null;
    }
    IndentationUtils.resetIndent();
    IndentationUtils.incrementTabMap();
    lineUps = new LinkedList<>();

    if (Objects.isNull(processibleFileList)) {
      return null;
    }

    for (File each : processibleFileList) {
      Class evaled = evalReturnClass(each.getPath());
      if (Objects.isNull(evaled)) {
        continue;
      }
      lineUps.offer(evaled);
    }

    /**
     * Annotation's insertion.
     * BFS to process class superFields,
     * and inner classes , and superFields in them..
     */
    List<DecorationLocalField> superFields = new ArrayList<>();
    while (!lineUps.isEmpty()) {
      try {
        Class popped = lineUps.poll();
        if (Objects.isNull(popped)) {
          continue;
        }
        if (rawInput.getWithClassHierarchy() && !visited.contains(cleanseClassPath(popped.getName()))) {
          /**
           * PHASE 1
           * Evaluate constructor if needs parent-merging.
           * This phase will only write a new ctor if need be.
           */
          List<Class> hierarchicalTopDownClasses = ReflectionUtils.buildTopDownClassesFrom(popped, Boolean.TRUE);
          /**
           * Add parent key to indicate a class has at least 1 child.
           */
          for (int i = 0, n = hierarchicalTopDownClasses.size(); i < n - 1; i++) {
            BaseConstructorPhaseAlgorithm.addChildren(hierarchicalTopDownClasses.get(i).getName(), "dummy", Boolean.TRUE);
          }
          for (int i = 0, n = hierarchicalTopDownClasses.size(); i < n; i++) {
            Class        curr          = hierarchicalTopDownClasses.get(i);
            final String classPathName = cleanseClassPath(curr.getName());
            if (visited.contains(classPathName)) {
              continue;
            }
            if (i > 0) {
              /**
               * Get super fields from previous class ( the direct parent - i - 1 in this case).
               */
              Class previouslyProcessed =  hierarchicalTopDownClasses.get(i - 1);
              /**
               * Put to a map to check if it is inherited.
               */
              superFields = BaseConstructorPhaseAlgorithm.getFieldsByClassName(previouslyProcessed.getName());
              BaseConstructorPhaseAlgorithm.addFieldListToMap(classPathName, superFields, Boolean.TRUE);
              BaseConstructorPhaseAlgorithm.addChildren(previouslyProcessed.getName(), classPathName, Boolean.TRUE);
            }
            enqueueWith(curr);
            processTrusted(curr, Boolean.FALSE);
            visited.add(classPathName);
          }
        }
        /**
         * After merging super - child fields
         */
        if (!visited.contains(cleanseClassPath(popped.getName()))) {
          executeNode(popped);
          visited.add(cleanseClassPath(popped.getName()));
        }
        if (ReflectionUtils.hasInnerClasses(popped)) {
          /**
           * put all inner classes to queue.
           */
          enqueueWithClassList(popped.getDeclaredClasses());
        }
        /**
         * ONLY IF REQUIRED
         * Collect all domain-based imported classes here enqueue then process.
         */
        if (rawInput.getBfsImports()) {
          try {
            String extractedContent = BaseConstructorPhaseAlgorithm.extractClassContent(popped);
            enqueueWithImportedDomainClasses(extractedContent);
          } catch (Throwable t) {
            throw new RuntimeException("rip");
          }
        }
        /**
         * ok process each domain-based field.
         */
        if (rawInput.getBfsFields()) {
          for (Field field : popped.getDeclaredFields()) {
            String fieldTypeName = field.getType().getTypeName();
            if (field.getType().isPrimitive() || Modifier.isTransient(field.getModifiers()) || visited.contains(
                    fieldTypeName)) {
              continue;
            }
            /**
             * eval generic inside
             */
            String[]      splitted             = field.getGenericType().toString().split("<");
            Class         currentNode          = null;
            Queue<String> concurrentModifiable = new LinkedList<>();
            concurrentModifiable = divideAndConquerBy(splitted, concurrentModifiable, ',');
            while (!concurrentModifiable.isEmpty()) {
              String eachRaw = concurrentModifiable.poll();
              if (visited.contains(cleanseClassPath(eachRaw)) || eachRaw.contains("slf4j")) {
                continue;
              }
              try {
                /**
                 * if this class uses an inner class...
                 */
                if (eachRaw.contains("$")) {
                  /**
                   * Add to this list, it will process this.
                   */
                  concurrentModifiable.offer(StringUtils.stripUntilDollarSign(eachRaw));
                }
                currentNode = Class.forName(eachRaw);
                if (Objects.isNull(currentNode) || lineUps.contains(currentNode)) {
                  continue;
                }
                //TODO verify
//              visited.add(cleanseClassPath(currentNode.getName()));
                if (ReflectionUtils.hardCodeIsJackson(currentNode)) {
                  continue;
                }
                if (ReflectionUtils.hasInnerClasses(currentNode)) {
                  /**
                   * put all inner classes to queue.
                   */
                  enqueueWithClassList(currentNode.getDeclaredClasses());
                }
              } catch (Throwable t) {
                try {
                  visited.add(currentNode.getName());
                } catch (Throwable e) {
                } //ok you can go
              }
              enqueueWith(currentNode);
            }
          }
        }
        if (visited.contains(popped.getName()) || ReflectionUtils.hardCodeIsJackson(popped)) {
          continue;
        }
        /**
         * If no field is "good"
         */
        executeNode(popped);
        visited.add(cleanseClassPath(popped.getName()));
      } catch (Throwable t) {
        throw new RuntimeException("rip");
      }
    }
    lineUps = new LinkedList<>();
    return new ExecutionResult(filesProcessed, fileCount);
  }

  private void executeNode(Class popped) {
    try {
      processTrusted(popped, Boolean.FALSE);
    } catch (Throwable t) {
      //do nothing
    }
  }

  /**
   * return a list of raw class paths
   * @param splitted
   * @return
   */
  public static Queue<String> divideAndConquerBy(String [] splitted, Queue<String> running, char by) {
    for (String each : splitted) {
      if (each.contains(String.valueOf(by))) {
        return divideAndConquerBy(each.split(","), running, by);
      }
      if (StringUtils.containsAny(each, "java.util", "java.lang") || running.contains(StringUtils.stripDoubleEndedNonAlphaNumeric(each))) {
        continue;
      }
      for (String inner : splitted) {
        if (!inner.contains(".") || StringUtils.containsAny(inner, "java.util", "java.lang") || running.contains(inner)) {
          continue;
        }
        running.offer(cleanseClassPath(inner));
      }
    }
    return running;
  }

  public static String cleanseClassPath(String raw) {
    String resolved = StringUtils
            .stripUntilClassPath(StringUtils
                            .stripDoubleEndedNonAlphaNumeric(StringUtils
                                    .resolveReplaces(raw,
                                            "class ",
                                            "",
                                            ">",
                                            "",
                                            "<",
                                            "", "[", "", "]", "", "[]","")),
                    '.', '$', '_');
    /**
     * Ok by far the fishiest logic for weird class strings here
     */
    if (resolved.contains("L") && resolved.indexOf(".") < resolved.indexOf("L")) {
      return resolved;
    }
    return StringUtils.resolveReplaces(resolved, "L", "");
  }

  private void enqueueWithImportedDomainClasses(String content) {
    if (StringUtils.isEmpty(content)) {
      return;
    }
    String subbed = content.substring(0, content.indexOf("{"));
    String [] splitted = subbed.split(";");
    for (String each : splitted) {
      if (!each.contains("import") || each.contains("java.") || each.contains("org.") || each.contains("_")) {
        continue;
      }
      String importLine = each.split(" ")[1];
      try {
        if (importLine.contains(";")) {
          importLine = importLine.substring(0, importLine.indexOf(";"));
        }
        Class clazz = Class.forName(importLine);
        if (StringUtils.containsAny(importLine, "Builder", "Util") || ReflectionUtils.isForbidden(clazz, rawInput) || ReflectionUtils.hardCodeIsJackson(clazz)) {
          continue;
        }
        lineUps.offer(clazz);
      } catch (ClassNotFoundException e) {
        continue; //ok go
      }
    }
  }
  private void enqueueWithClassList(Class[] list) {
    for (Class clazz : list) {
      if (ReflectionUtils.isForbidden(clazz, rawInput) || ReflectionUtils.hardCodeIsJackson(clazz)) {
        continue;
      }
      if (visited.contains(StringUtils.stripDoubleEndedNonAlphaNumeric(clazz.getName()))) {
        continue;
      }
      enqueueWith(clazz);
    }
  }

  private Class evalReturnClass(String rawName) {
    Class clazz = ReflectionUtils.getClass(rawName);
    if (Objects.isNull(clazz)) {
      return null;
    }
    if (ReflectionUtils.isForbidden(clazz, rawInput) || ReflectionUtils.hardCodeIsJackson(clazz)) {
      return null;
    }
    return clazz;
  }
  /**
   * recursively add constructors
   * to inner classes
   *
   * @param inners
   */
  private void recursivelyProcessTrustedInnerClasses(List<Class> inners) {
    if (CollectionUtils.isEmpty(inners)) {
      LOG.warn("No inner classes !");
      return;
    }
    for (Class inner : inners) {
      if (Objects.isNull(inner)) {
        continue;
      }
      if (ReflectionUtils.hasInnerClasses(inner)) {
        recursivelyProcessTrustedInnerClasses(Arrays.asList(inner.getDeclaredClasses()));
      }
      processTrusted(inner, Boolean.TRUE);
    }
  }

  private void processTrusted(Class clazz, boolean isInner) {
    try {
      //oh yeah this is creepy
      if (isInner || clazz.getName().contains("$")) {
        if (!isInner) {
          isInner = true; //to reset indentation map
        }
        IndentationUtils.incrementTabMap();
        BaseConstructorPhaseAlgorithm.updateWith(clazz, true);
      } else {
        BaseConstructorPhaseAlgorithm.beginWith(clazz);
      }
      executePhases(InitialDummyPhase.emptyInstance());
    } catch (Throwable t) {
      LOG.info("!!!failed alert " + (isInner ? " inner " : "") + "class = " + clazz.getName());
      FileUtils.writeContentToFile("failed_classes.txt", "error  = " + t.toString() + "\nat " + (isInner ? " inner " : "") + "class = " + clazz.getName(), true);
    }
    if (isInner) {
      IndentationUtils.decrementTabMap();
    }
  }
}














