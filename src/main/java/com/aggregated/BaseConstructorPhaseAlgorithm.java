













package com.aggregated;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class BaseConstructorPhaseAlgorithm {
  private static final Logger LOG = LoggerFactory.getLogger(BaseConstructorPhaseAlgorithm.class);
  protected static final char OPEN_BRACKET = '{';
  protected static final char CLOSE_BRACKET = '}';
  protected static final char OPEN_PAREN = '(';
  protected static final char CLOSE_PAREN = ')';
  protected static final char SEMICOLON = ';';
  protected static final String IMPORT_KEYWORD = "import";
  protected static final String COM_KEYWORD = "com";
  protected static final char COLON = ':';
  protected static final char COMMA = ',';
  protected final RawClientRuleInput rawInput;
  protected static Class<?> CLAZZ;
  protected static final String SHOULD_ADD_DEFAULT_CTOR = "_should_add_default_ctor";
  /**
   * Top-level class acts as a cache
   * for when inner classes are modified.
   */
  protected static Class<?> TOP_LEVEL_CLAZZ;
  protected static String slashedFullName;
  protected static String CLASS_CONTENT;
  protected static String[] FIELD_REGION;
  protected static String RAW_FIELD_REGION;
  protected static int CLASS_KEYWORD_N_NAME_IDX;
  protected static int WRITABLE_CTOR_IDX;
  /**
   * If continuously fail to find.
   */
  protected static final int MAX_FAILED_ATTEMPTS = 3;
  protected static boolean IS_DEFAULT_CONSTRUCTOR;
  protected static final String ENDING_JAVA_EXT = ".java";
  protected static final String CLASS_KEYWORD = "class";
  protected static final String SPACE = " ";
  protected static final String PUBLIC_MOD = "public";
  protected static final String PRIVATE_MOD = "private";
  protected static final String PROTECTED_MOD = "protected";
  protected static final String FINAL_KEYWORD = "final";
  protected static final String NEW_KEYWORD = "new";
  protected static final String[] PREFIXES = {PUBLIC_MOD, PRIVATE_MOD, PROTECTED_MOD, ""};
  protected static List<Integer> internCtorIdxes;
  protected static Optional<Boolean> hasStringLevelDefaultCtor = Optional.empty();
  protected static DoubleEndedStack<PhaseChainedResult> stackedPhaseResults;
  protected static Map<String, List<DecorationLocalField>> classToMergeableParams;
  protected static Map<String, String> parentToChild = new HashMap<>();
  protected static Map<String, String> classToPath = new HashMap<>();
  protected BaseConstructorPhaseAlgorithm(RawClientRuleInput rawInput) {
    this.rawInput = rawInput;
  }
  protected static boolean isInnerClass() {
    return !TOP_LEVEL_CLAZZ.getSimpleName().equalsIgnoreCase(CLAZZ.getSimpleName());
  }

  public static Optional<Boolean> getHasStringLevelDefaultCtor() {
    if (hasStringLevelDefaultCtor.isPresent()) {
      return hasStringLevelDefaultCtor;
    }
    return null;
  }

  protected void pushStack(PhaseChainedResult result) {
    if (Objects.isNull(stackedPhaseResults)) {
      stackedPhaseResults = new DoubleEndedStack<>();
    }
    if (stackedPhaseResults.contains(result)) {
      return;
    }
    stackedPhaseResults.push(result);
  }

  /**
   * If the expected ordinal is > stack
   * then, return the top ( if any )
   *
   * @param ordinal the index of the pushed order.
   * @return
   */
  protected PhaseChainedResult fetchPhaseResult(int ordinal) {
    final int stackSize = stackedPhaseResults.size();
    if (ordinal > stackSize) {
      return stackedPhaseResults.pop();
    }
    stackedPhaseResults.dynamicPop(ordinal);
    if (!stackedPhaseResults.isEmpty()) {
      return stackedPhaseResults.pop();
    }
    return null;
  }

  public abstract PhaseChainedResult execute(PhaseChainedResult previousInput);

  public static void addChildren(String parentName, String childName, boolean forcePut) {
    if (NullabilityUtils.isAnyNullIn(parentName, childName)) {
      return;
    }
    if (Objects.isNull(parentToChild)) {
      parentToChild = new HashMap<>();
    }
    if (forcePut) {
      parentToChild.put(parentName, childName);
      return;
    }
    String toPut = childName;
    if (parentToChild.containsKey(parentName)) {
      toPut += COMMA + parentToChild.get(parentName);
    }
    parentToChild.put(parentName, toPut);
  }
  public static Map<String, String> getParentToChildMap() {
    return parentToChild;
  }

  public static void addFieldListToMap(String className, List<DecorationLocalField> annotatedParams, boolean forcePut) {
    if (Objects.isNull(classToMergeableParams)) {
      classToMergeableParams = new HashMap<>();
    }
    if ((!forcePut && classToMergeableParams.containsKey(className)) || CollectionUtils.isEmpty(annotatedParams)) {
      return;
    }
    classToMergeableParams.put(className, annotatedParams);
  }

  public static List<DecorationLocalField> getFieldsByClassName(String className) {
    if (Objects.isNull(classToMergeableParams) || !classToMergeableParams.containsKey(className)) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(classToMergeableParams.get(className));
  }

  public static void addPathToClass(String className, String path) {
    if (Objects.isNull(classToPath)) {
      classToPath = new HashMap<>();
    }
    if (className.charAt(0) == '.') {
      return;
    }
    classToPath.put(StringUtils.correctifyImportString(className, '.'), path);
  }

  public static String getFullPathForClass(String className, boolean isCloseMatch) {
    if (!isCloseMatch) {
      return getFullPathForClass(className);
    }
    if (MapUtils.isEmpty(classToPath)) {
      return "";
    }
    for (Map.Entry<String, String> entry : classToPath.entrySet()) {
      String key = entry.getKey();
      if (key.contains(className) || className.contains(key)) {
        return classToPath.get(key);
      }
    }
    return "";
  }
  public static String getFullPathForClass(String className) {
    if (MapUtils.isEmpty(classToPath) || !classToPath.containsKey(className)) {
      return "";
    }
    return StringUtils.stripDoubleEndedNonAlphaNumeric(classToPath.get(className));
  }

  public static void beginWith(Class fromClazz) {
    stackedPhaseResults = new DoubleEndedStack<>();
    LOG.info("Processing class = " + fromClazz.getName());
    CLAZZ = fromClazz;
    TOP_LEVEL_CLAZZ = fromClazz;
    CLASS_CONTENT = extractClassContent(null);
    if (Objects.isNull(CLASS_CONTENT)) {
      return;
    }
    hasStringLevelDefaultCtor = Optional.empty();
    reprocessVitals();
  }
  public static void makeFieldRegion() {
    int newFrom = CLASS_CONTENT.indexOf("{", CLASS_KEYWORD_N_NAME_IDX);
    int newTo = CLASS_CONTENT.indexOf(";", WRITABLE_CTOR_IDX);
    RAW_FIELD_REGION = CLASS_CONTENT.substring(newFrom, newTo);
    FIELD_REGION = RAW_FIELD_REGION.split(" ");
  }

  /**
   * All inner declared classes
   * already belong to the top-level class.
   * So just process what needs updating.
   */
  public static void updateWith(Class clazz, boolean isUpdateTopLvlClass) {
    /**
     * Always start with a top-level class.
     * Because we gradually write
     * new constructor code from top to bottom.
     */
    LOG.info("Processing class = " + clazz.getName());
    stackedPhaseResults = new DoubleEndedStack<>();
    CLAZZ = clazz;
    if (isUpdateTopLvlClass) {
      try {
        TOP_LEVEL_CLAZZ = Class.forName(StringUtils.stripUntilDollarSign(clazz.getName()));
      } catch (ClassNotFoundException e) {
        return; //chill and return
      }
    }
    CLASS_CONTENT = extractClassContent(null);
    if (Objects.isNull(CLASS_CONTENT)) {
      return;
    }
    hasStringLevelDefaultCtor = Optional.empty();
    reprocessVitals();
  }

  /**
   * call when class content changes,
   * new annotation is added for e.g
   * or at the initial phase
   * Update writable ctor idx
   * class keyword and idx,
   * class content.
   */
  protected static void reprocessVitals() {
    try {
      CLASS_KEYWORD_N_NAME_IDX =
          forceFindExactPrefixedString(0, CLASS_CONTENT, CLAZZ.getSimpleName(), CLASS_KEYWORD, 1, OPEN_BRACKET);
      WRITABLE_CTOR_IDX = findStartForNewCtor(CLASS_KEYWORD_N_NAME_IDX, CLASS_CONTENT);
      makeFieldRegion();
      internCtorIdxes = buildExistingCtorList();
    } catch (Throwable t) {
      return; //chill out
    }
  }

  /**
   * Stateless
   * @return
   */
  protected static String[] getFieldRegion() {
    return FIELD_REGION;
  }

  protected static int getEndingImportRegionIndex() {
    return Math.max(
        forceFindPrefixedString(Optional.of(0),
                                IMPORT_KEYWORD,
                                COM_KEYWORD,
                                CLASS_CONTENT.substring(0, CLASS_KEYWORD_N_NAME_IDX - 1),
                                Optional.of(-1)),

        forceFindPrefixedString(Optional.of(0),
                                IMPORT_KEYWORD,
                                "",
                                CLASS_CONTENT.substring(0, CLASS_KEYWORD_N_NAME_IDX - 1),
                                Optional.of(-1)));
  }
  /**
   * Stateful
   * @return
   */
  protected static String getImportRegion() {
    final int lastIdxOfImportLine = getEndingImportRegionIndex();
    if (lastIdxOfImportLine == -1) {
      return "";
    }
    return CLASS_CONTENT.substring(0, CLASS_CONTENT.indexOf(SEMICOLON, lastIdxOfImportLine) + 1);
  }

  public static String extractClassContent(Class given) {
    Class revert = TOP_LEVEL_CLAZZ;
    if (Objects.nonNull(given)) {
      TOP_LEVEL_CLAZZ = given;
    }
    slashedFullName =
        InputReceiver.BASE_SOURCE + TOP_LEVEL_CLAZZ.getPackage().getName() + "." + TOP_LEVEL_CLAZZ.getSimpleName();
    slashedFullName = StringUtils.resolveReplaces(slashedFullName, ".", "\\\\");
    if (!slashedFullName.endsWith(ENDING_JAVA_EXT)) {
      slashedFullName += ENDING_JAVA_EXT;
    }
    Path path        = Paths.get(slashedFullName);
    byte[] fileBytes = new byte[0];
    try {
      fileBytes = Files.readAllBytes(path);
    } catch (Throwable t) {
      return null;
    }
    TOP_LEVEL_CLAZZ = revert;
    return new String(fileBytes);
  }

  protected static boolean isValidCtor(int idx) {
    String checkScope = CLASS_CONTENT.substring(CLASS_CONTENT.indexOf(CLAZZ.getSimpleName(), idx),
                                                CLASS_CONTENT.indexOf(CLOSE_PAREN, idx));
    checkScope = StringUtils.stripDoubleEndedNonAlphaNumeric(checkScope).substring(CLAZZ.getSimpleName().length());
    for (int i = 0, n = checkScope.length(); i < n; i++) {
      Character c = checkScope.charAt(i);
      if (c == OPEN_PAREN) {
        return true;
      }
      if (Character.isLetterOrDigit(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * m : declared constructor's size.
   * n : access_modifier list's size
   *
   * time complexity : O(c1 x c2 x n)
   * space : O(n)
   * @return
   */
  protected static List<Integer> buildExistingCtorList() {
    List<Integer> existingCtors = new ArrayList<>();
    int declaredCtors = CLAZZ.getDeclaredConstructors().length;

    try {
      for (int i = 0, n = PREFIXES.length; i < n; i++) {
        int start = CLASS_KEYWORD_N_NAME_IDX;
        for (int j = 0, m = declaredCtors; j < m; j++) {
          int internIdx = -1;
          internIdx = forceFindExactPrefixedString(start,
                                                   CLASS_CONTENT,
                                                   CLAZZ.getSimpleName(),
                                                   PREFIXES[i],
                                                   1,
                                                   OPEN_PAREN);

          if (internIdx == -1) {
            continue;
          }
          String seeminglyAccessMod =  StringUtils.findPrependablePieceFrom(
              CLASS_CONTENT,
              internIdx,
              '\r',
              false);

          if (existingCtors.contains(internIdx) || !isValidCtor(internIdx)
              || seeminglyAccessMod.contains(NEW_KEYWORD) || (PREFIXES[i].isEmpty() && isAnyAnagramFoundIn(PREFIXES, seeminglyAccessMod))) {
            start = internIdx + 1;
            continue;
          }
          /**
           * Eval if any string-level ctor found
           */
          if (!hasStringLevelDefaultCtor.isPresent()) {
            int openParenIdx  = CLASS_CONTENT.indexOf(OPEN_PAREN, internIdx);
            int closeParenIdx = CLASS_CONTENT.indexOf(CLOSE_PAREN, openParenIdx);
            String declaredParm = CLASS_CONTENT
                .substring(openParenIdx + 1, closeParenIdx);
            if (StringUtils.isEmpty(declaredParm)) {
              hasStringLevelDefaultCtor = Optional.of(Boolean.TRUE);
            }
          }
          existingCtors.add(internIdx);
          start = CLASS_CONTENT.indexOf(CLOSE_BRACKET, internIdx);
        }
      }
    } catch (Throwable t) {
      //do nothing
    }
    if (CollectionUtils.isEmpty(existingCtors)) {
      existingCtors.add(-1);
//      /**
//       * Haiz
//       */
//      if (ReflectionUtils.hasMutator(CLAZZ) && !hasStringLevelDefaultCtor.isPresent()) {
//        hasStringLevelDefaultCtor = Optional.of(Boolean.TRUE);
//      }
    }
    if (!hasStringLevelDefaultCtor.isPresent()) {
      hasStringLevelDefaultCtor = Optional.of(Boolean.FALSE);
    }
    return existingCtors;
  }

  /**
   * Supposed this access-mod-ed ctor:
   *    public Person(...
   *
   *   The next ctor's index will be on the same line.
   *   -> use only one.
   *
   *  could use a matrix, adjacency list to query based on row , col stuffs but
   *  this saves memory.
   * @param list
   * @param inp
   * @return
   */
  private static boolean isAnyAnagramFoundIn(String[] list, String inp) {
    for (String each : list) {
      if (!StringUtils.isNoneBlank(inp) || !StringUtils.isNoneBlank(inp)) {
        continue;
      }
      if (StringUtils.isAnagram(each, inp)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Forcefully find prefixed (exact match) with the given prefix,
   * regardless of middle spaces.
   *
   * @param startAt
   * @param content
   * @param prefixed         is the word after prefix (prefixed by the "prefix")
   * @param prefix           the prefix
   * @param ordinal          the nth-matched result
   *                         -1 means find the last possible match
   * @param endingRegionChar : the ending boundary for the extracted range to validate against.
   * @return
   */
  protected static int forceFindExactPrefixedString(int startAt,
                                                    String content,
                                                    String prefixed,
                                                    String prefix,
                                                    int ordinal,
                                                    char endingRegionChar) {
    int res = -1;
    int failedAttempts = -1;
    StringBuilder exactMatch = new StringBuilder();
    while (content.length() > 0 && content.indexOf(prefix) != -1) {
      if (res == -1) {
        failedAttempts++;
        if (failedAttempts == MAX_FAILED_ATTEMPTS) {
          break;
        }
        res = forceFindPrefixedString(Optional.of(startAt), prefix, prefixed, content, Optional.of(ordinal));
        continue;
      }
      /**
       * Double check
       */
      int endingRegionCharIdx = content.indexOf(endingRegionChar, res);
      if (endingRegionCharIdx == -1) {
        break;
      }
      String extractedRange = content.substring(res, endingRegionCharIdx);
      /**
       * Validate the extracted range
       * to skip noises.
       * FOR METHODS ( constructors included ) only.
       */
      if (!CLASS_KEYWORD.equalsIgnoreCase(prefix)) {
        String[] mayContainNoises = extractedRange.split(" ");
        int goodValCounter = 0;
        for (int i = 0, n = mayContainNoises.length; i < n; i++) {
          String each = mayContainNoises[i];
          if (StringUtils.isEmpty(each)) {
            continue;
          }
          if (Character.isLetterOrDigit(each.charAt(0)) || each.charAt(0) == '=') {
            goodValCounter++;
          }
        }
        /**
         * we're only interested 2 ( prefix and prefixed),
         * otherwise, reset
         */
        if (goodValCounter > 2) {
          startAt = res + 1;
          res = -1;
          continue;
        }
      }
      /**
       * Safe to go.
       */
      int prefixIdx = extractedRange.indexOf(prefix);
      if (prefixIdx == -1) {
        break;
      }
      extractedRange =
          StringUtils.stripDoubleEndedNonAlphaNumeric(
              extractedRange.substring(extractedRange.indexOf(prefix) + prefix.length() + 1, extractedRange.length()));

      if (extractedRange.contains(SPACE) || extractedRange.contains("<")) {
        extractedRange = extractedRange.split(SPACE)[0];
        int idx = StringUtils.firstIdxOfNonAlphanumeric(extractedRange);
        if (idx != -1) {
          extractedRange = extractedRange.substring(0, idx);
        }
      }
      exactMatch.setLength(0);
      /**
       * Handle exact matches,
       * "Persons" won't be returned in place of "Person".
       */
      for (int i = extractedRange.indexOf(prefixed), n = extractedRange.length(); i < n; i++) {
        if (Character.isWhitespace(extractedRange.charAt(i))) {
          if (exactMatch.toString().equalsIgnoreCase(prefixed)) {
            return res;
          }
          break;
        }
        exactMatch.append(extractedRange.charAt(i));
      }
      if (exactMatch.toString().equalsIgnoreCase(prefixed)) {
        return res;
      }
      startAt = res + prefixed.length();
      res = -1;
    }
    return res;
  }

  public final boolean shouldSkipCurrentClass() {
    final String className = CLAZZ.getSimpleName();
    final List<String> skippedClasses = rawInput.getSkipClasses();
    for (String eachSkipped : skippedClasses) {
      if (StringUtils.containsAny(className, eachSkipped) || StringUtils.endsWithAny(className,
                                                                                     eachSkipped)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Find the n-th expected <prefix - multi spaces - prefixed> in the given content.
   *
   * @param startAt
   * @param prefix
   * @param prefixed
   * @param classContent
   * @param ordinal      -1 means find the last possible match.
   * @return last possible match (if any), if failed the n-th.
   */
  protected static final int forceFindPrefixedString(Optional<Integer> startAt,
                                                     String prefix,
                                                     String prefixed,
                                                     String classContent,
                                                     Optional<Integer> ordinal) {
    int res = -1;
    int successTimes = 0;
    int attempts = -1;
    int internRes = -1;
    String spaceQty = "";
    if (!startAt.isPresent()) {
      startAt = Optional.of(0);
    }
    if (!ordinal.isPresent()) {
      ordinal = Optional.of(1);
    }
    do {
      attempts++;
      /**
       * bail if even a 3-spaced value is absent
       */
      if (attempts == MAX_FAILED_ATTEMPTS) {
        /**
         * Squeeze if more values,
         */
        if (classContent.indexOf(prefix + spaceQty + prefixed, res + 1) != -1) {
          attempts = 0;
          continue;
        }
        return internRes != -1 ? internRes : res; //maybe ugly code
      }
      if (res == -1) {
        successTimes = 0;
      } else {
        successTimes++;
        if (successTimes == ordinal.get()) {
          return internRes != -1 ? internRes : res;
        }
        internRes = res;
        /**
         * Otherwise, update to find the n-th match
         */
        spaceQty = "";
        startAt = Optional.of(res + 1);
      }
      spaceQty += " "; //expand the space every time
      res = classContent.indexOf(prefix + spaceQty + prefixed, startAt.get());
    } while (true);
  }

  /**
   * Return the index which is just below the parameters
   * or after class's declaration if no param.
   *
   * @param CLASS_CONTENT
   * @return Constraint(s):
   * class should have >= 1 declared method.
   * Idea :
   * if >= 2 brackets, subrange = 1st brk to 2nd brk
   * if no ; found within this range : insert line break + default ctor
   * if ; , find the last ; , then insert defautl ctor
   * <p>
   * if < 2 brckts,
   * sub-range = bracket to length
   * if ; , find the last, then insert
   * else , append
   */
  private static int findStartForNewCtor(int toFindFromIdx, String CLASS_CONTENT) {
    if (toFindFromIdx == -1) {
      return -1;
    }
    int semicolonIdx = -1;
    int closeBrktIdx = -1;
    int seeminglyClassOpBrkt = CLASS_CONTENT.indexOf(OPEN_BRACKET, toFindFromIdx + 1);
    //loop from the first bracket onward.
    for (int i = seeminglyClassOpBrkt + 1, n = CLASS_CONTENT.length(); i < n; i++) {
      if (CLASS_CONTENT.charAt(i) == SEMICOLON) { //just update the running semi-colon index
        semicolonIdx = i;
      } else if (CLASS_CONTENT.charAt(i) == CLOSE_BRACKET) {
        if (semicolonIdx != -1) {
          return semicolonIdx;
        }
        closeBrktIdx = i;
      } else if (CLASS_CONTENT.charAt(i) == OPEN_BRACKET) {
        /**
         * If at least CLOSE BRACKET is found,
         * it means we met ANNOTATIONS,
         * so must continue.
         */
        if (closeBrktIdx != -1) {
          closeBrktIdx = -1; //reset to escape this code the 2nd time.
          i++;
          continue;
        }
        break;
      }
    }
    return semicolonIdx == -1 ? seeminglyClassOpBrkt : semicolonIdx;
  }

}
