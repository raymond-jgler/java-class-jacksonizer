package com.aggregated;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
  private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);

  private static final String OPEN_PAREN   = "(";
  private static final String CLOSE_PAREN  = ")";
  private static final String AT           = "@";
  private static final String COMMA        = ",";
  private static final String DOT          = ".";
  private static final String EQUAL        = " = ";
  private static final String SPACE        = " ";
  private static final char   SEMICOLON    = ';';
  private static final String SINGLE_BREAK = "\n";
  private final static Map<String, String> rawImportToResovled = new HashMap<>();

  public static boolean isNotEmpty(String input) {
    return !isEmpty(input);
  }

  public static boolean isEmpty(String input) {
    return Objects.isNull(input) || input.isEmpty();
  }

  public static boolean isNoneBlank(String input) {
    if (isEmpty(input)) {
      return false;
    }
    for (Character each : input.toCharArray()) {
      if (Character.isLetterOrDigit(each)) {
        return true;
      }
    }
    return false;
  }

  public static boolean containsAny(String toCheck, String... args) {
    for (String each : args) {
      if (toCheck.contains(each) || toCheck.contains(each) || each.contains(toCheck) || each.contains(toCheck)) {
        return true;
      }
    }
    return false;
  }

  public static String resolveReplaces(String orig, String... fromToPairs) {
    final int PAIR_JUMP = 2;
    if (fromToPairs.length % 2 != 0) {
      LOG.error("Not enough data to perform action");
    }
    for (int i = 0, n = fromToPairs.length; i < n; i += PAIR_JUMP) {
      orig = orig.replace(fromToPairs[i], fromToPairs[i + 1]);
    }
    //fishy, ensure single-dotted only.
    return orig.replaceAll("\\.+", ".");
  }

  public static boolean endsWithAny(String toCheck, String... args) {
    for (String each : args) {
      if (toCheck.endsWith(each) || toCheck.endsWith(each + ".java")) {
        return true;
      }
    }
    return false;
  }

  public static String stripComments(String inp) {
    inp = inp.replaceAll("//.*", "");
    Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(inp);
    inp = matcher.replaceAll("");

    return inp;
  }

  public static String appendIndentableBracketTo(String inp, String bracket, String indentVal) {
    if (inp.isEmpty() || inp.contains(String.valueOf(bracket))) {
      return inp;
    }
    String res = inp;
    if (!bracket.equalsIgnoreCase(String.valueOf(res.charAt(res.length() - 1)))) {
      res += indentVal + bracket;
    }
    return res;
  }

  public static String stripUntilDollarSign(String inp) {
    for (int i = 0, n = inp.length(); i < n; i++) {
      if (inp.charAt(i) == '$') {
        return inp.substring(0, i);
      }
    }
    return inp;
  }

  public static String stripUntilClassPath(String inp, Character... toKeep) {
    Set<Character> toKeeps = new HashSet<>(Arrays.asList(toKeep));
    StringBuilder  sb      = new StringBuilder();
    for (Character c : inp.toCharArray()) {
      if (Character.isLetterOrDigit(c) || toKeeps.contains(c)) {
        sb.append(c);
      }
    }
    return resolveReplaces(sb.toString(), "/", "");
  }

  public static boolean isAllLowerCase(String inp) {
    for (Character c : inp.toCharArray()) {
      if (Character.isUpperCase(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Pincer-strip double-ended non alphanumeric chars from string,
   * until meets character / digit from both ends.
   *
   * @param inp
   * @return
   */
  public static String stripDoubleEndedNonAlphaNumeric(String inp) {
    final int  THRESHOLD = 200;
    final long start     = System.currentTimeMillis();
    int        left      = 0, n = inp.length() - 1, right = n;
    while (left < right && left < inp.length() && !Character.isLetterOrDigit(inp.charAt(left))) {
      left++;
    }
    while (left < right && right > 0 && !Character.isLetterOrDigit(inp.charAt(right))) {
      right--;
    }
    //if unchanged.
    if (left >= right || (left == 0 && right == n)) {
      return inp;
    }

    while (true) {
      if (System.currentTimeMillis() - start >= THRESHOLD) {
        break;
      }
      try {
        return inp.substring(left, right + 1);
      } catch (Throwable t) {
        right -= 1;
      }
    }
    return inp;
  }

  public static int lastIndexOf(String inp, char x, Integer backwardFrom, Integer ordinalIndex, Boolean skipBreaker) {
    if (Objects.isNull(skipBreaker)) {
      skipBreaker = true;
    }
    if (Objects.isNull(ordinalIndex)) {
      ordinalIndex = 1;
    }
    int n = inp.length() - 1;
    if (Objects.nonNull(backwardFrom)) {
      n = backwardFrom;
    }
    int matches    = 0;
    int shrinkingI = n;
    for (int i = n; i >= 0; i--) {
      if (inp.charAt(i) == x) {
        matches++;
        if (ordinalIndex == -1) {
          shrinkingI = i;
          continue;
        }
        if (matches == ordinalIndex) {
          return i;
        }
      }
      if ((skipBreaker) && (inp.charAt(i) == '\r' || inp.charAt(i) == '\n')) {
        break;
      }
    }
    return shrinkingI;
  }

  public static int countCharsFromEnd(String inp, char x) {
    int i     = lastIndexOf(inp, x, null, null, null);
    int count = 0;
    while (i >= 0 && inp.charAt(i) == x) {
      i--;
      count++;
    }
    return count;
  }

  public static List<String> makeNonAlphaStringsFrom(String inp) {
    StringBuilder sb  = new StringBuilder();
    List<String>  res = new ArrayList<>();
    for (Character c : inp.toCharArray()) {
      if (Character.isLetterOrDigit(c)) {
        sb.append(c);
        continue;
      }
      if (sb.length() > 0) {
        res.add(sb.toString());
        sb.setLength(0);
      }
    }
    if (sb.length() > 0) {
      res.add(sb.toString());
    }
    return res;
  }

  public static String bulkCascadeRemoveSuffixedString(String inp, char suffix, Character... patternSplitterTeller) {
    final List<Character> teller             = Arrays.asList(patternSplitterTeller);
    StringBuilder         partitionCollector = new StringBuilder();
    StringBuilder         removed            = new StringBuilder();
    for (int i = 0, n = inp.length(); i < n; i++) {
      Character cur = inp.charAt(i);
      if (!teller.contains(cur)) {
        partitionCollector.append(cur);
        continue;
      }
      Character connective = cur;
      if (i == n - 1) {
        if (partitionCollector.length() > 0) {
          removed.append(cascadeRemoveSuffixedString(partitionCollector.toString(), suffix));
        }
        removed.append(connective);
        break;
      }
      removed.append(cascadeRemoveSuffixedString(partitionCollector.toString(), suffix));
      if (Objects.nonNull(connective)) {
        removed.append(connective);
      }
      partitionCollector.setLength(0);
    }
    String finalSwticher = removed.length() == 0 ? partitionCollector.toString() : removed.toString();
    if (finalSwticher.contains(String.valueOf(suffix))) {
      finalSwticher = cascadeRemoveSuffixedString(finalSwticher, suffix);
    }
    return finalSwticher;
  }

  /**
   * Should be used when ony " ONE pattern range " is present.
   * This method will work for only 1 self-contained pattern.
   * not work for multiple pattern ranges.
   *
   * A suffixed string is a pattern containing:
   * a word followed by a character.
   * for example, these are suffixed strings:
   * string =  java.util.List
   * suffixed strings : java., util.
   * non-suffixed : List
   *
   * @param inp
   * @return a string having its suffixed ones removed.
   *
   * input : java.util.List
   * output : List
   */
  public static String cascadeRemoveSuffixedString(String inp, char suffix) {
    if (!inp.contains(String.valueOf(suffix))) {
      return inp;
    }
    int n          = inp.length();
    int i          = n - 1;
    int rightBound = StringUtils.lastIndexOf(inp, suffix, i, 1, null);
    int leftBound  = StringUtils.lastIndexOf(inp, suffix, i, -1, null);
    /**
     * 2 cases for words preceding leftBound
     * _ a whole removable string
     * _ a partial removable string ( blocked by other words ).
     */
    i = leftBound - 1;
    for (; i >= 0 && Character.isLetterOrDigit(inp.charAt(i)); i--) {
    }
    leftBound = i;

    return StringUtils.ripRangeFromString(inp, leftBound, rightBound);
  }

  public static String ripRangeFromString(String inp, int exceptFrom, int exceptTo) {
    StringBuilder ripped = new StringBuilder();
    for (int i = 0, n = inp.length(); i < n; i++) {
      /**
       * Exclusive left bound
       * Inclusive right bound.
       */
      if (i > exceptFrom && i <= exceptTo) {
        continue;
      }
      ripped.append(inp.charAt(i));
    }
    return ripped.toString();
  }

  public static boolean isPrefixedWith(String prefix, String content) {
    int           n         = content.length();
    StringBuilder revRunner = new StringBuilder();
    for (int i = n - 1; i >= 0; i--) {
      Character cur = content.charAt(i);
      if (Character.isLetter(cur)) {
        revRunner.append(cur);
      }
      if (revRunner.length() == prefix.length()) {
        return revRunner.reverse().toString().equalsIgnoreCase(prefix);
      }
    }
    return false;
  }

  public static int firstIdxOfNonAlphanumeric(String x) {
    for (int i = 0, n = x.length(); i < n; i++) {
      if (Character.isLetterOrDigit(x.charAt(i))) {
        continue;
      }
      return i;
    }
    return -1;
  }

  public static String buildAnnotationPackage(String unresolvedPackage, String annotation) {
    return (SPACE + unresolvedPackage + DOT + annotation + SEMICOLON).replace(AT, "");
  }

  /**
   * Will stop when reaching the last separator.
   *
   * @param inp
   * @param separator
   * @return
   */
  public static String getLastWord(String inp, String separator) {
    NullabilityUtils.isAllNonEmpty(true, inp, separator);
    StringBuilder rev = new StringBuilder();
    for (int n = inp.length(), i = n - 1; i >= 0; i--) {
      Character cur = inp.charAt(i);
      if (separator.equalsIgnoreCase(String.valueOf(cur))) {
        break;
      }
      rev.append(cur);
    }
    return rev.reverse().toString();
  }

  /**
   * Separated by each dot,
   * ensure no more than 1 word contains >= 1 upper-case characters.
   * @param inp
   * @return
   */
  public static String correctifyImportString(String inp, Character sep) {
    if (StringUtils.isEmpty(inp) || !inp.contains(String.valueOf(sep))) {
      return inp;
    }
    if (!MapUtils.isEmpty(rawImportToResovled) && rawImportToResovled.containsKey(inp)) {
      return rawImportToResovled.get(inp);
    }
    StringBuilder res  = new StringBuilder();
    StringBuilder each = new StringBuilder();
    boolean isMetUppercase = false;
    for (int i = 0, n = inp.length(); i < n && !isMetUppercase; i++) {
      Character curr = inp.charAt(i);
      if (curr != sep) {
        each.append(curr);
        continue;
      }
      if (each.length() > 0 && !res.toString().contains(each)) {
        if (res.length() > 0) {
          res.append(sep);
        }
        res.append(each);
        if (!isAllLowerCase(each.toString())) {
          isMetUppercase = true;
        }
      }
      each.setLength(0);
    }
    if (each.length() > 0 && !res.toString().contains(each)) {
      res.append(sep)
              .append(each);
    }
    /**
     * Ok time to hack
     */
    String toPut = "";
    if (inp.charAt(0) == '.' || inp.charAt(inp.length() - 1) == '.') {
      final String rawText = stripDoubleEndedNonAlphaNumeric(inp);
      toPut = "java.util." + rawText;
    }
    rawImportToResovled.put(inp, toPut);
    return rawImportToResovled.get(inp);
  }

  public static boolean bidirectionalContains(String x, String y) {
    return x.contains(y) || y.contains(x);
  }

  public static int firstIndexOf(String inp, char x, int start, boolean isBackward) {
    if (isBackward) {
      for (int i = start; i >= 0; i--) {
        if (x == inp.charAt(i)) {
          return i;
        }
      }
    } else {
      for (int i = start, n = inp.length(); i < n; i++) {
        if (x == inp.charAt(i)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Space is handled by the caller.
   * @param key
   * @param value
   * @param operator
   * @return
   */
  public static String formKeyValuePair(String key, String value, String operator) {
    NullabilityUtils.isAllNonEmpty(true, key, value, operator);
    return key + operator + value;
  }

  public static String findPrependablePieceFrom(String content, int backwardFrom, Character breakingChar, boolean isSkipSpace) {
    if (Objects.isNull(breakingChar)) {
      breakingChar = '\r';
    }
    StringBuilder rev = new StringBuilder();
    for (int i = backwardFrom; i >= 0; i--) {
      Character c = content.charAt(i);
      if (String.valueOf(content.charAt(i)).equalsIgnoreCase(SINGLE_BREAK) || content.charAt(i) == breakingChar) {
        break;
      }
      if (isSkipSpace && !Character.isLetterOrDigit(c)) {
        continue;
      }
      rev.append(c);
    }
    return rev.reverse().toString();
  }

  /**
   * @param c must be alphanumeric.
   * @param isRebounce use 0 as index.
   * @return
   */
  public static int asciiValueOf(char c, boolean isRebounce) {
    int asciiVal = -1;
    if (!isRebounce || !Character.isLetter(c)) {
      return c;
    }
    if (Character.isLowerCase(c)) {
      asciiVal = c - 97;
    } else {
      asciiVal = c - 65;
    }
    return asciiVal;
  }

  /**
   * Supports upper/lower-cased alphanumeric letters.
   * @param x
   * @param y
   * @return
   */
  public static boolean isAnagram(String x, String y) {
    /**
     * Set A = {Aa-Zz + 0 -> 9} -> nO of chars =  62
     */
    int [] map1 = new int[62];
    int [] map2 = new int[62];
    Arrays.fill(map1, 0);
    Arrays.fill(map2, 0);
    int i = 0;
    int n = x.length();
    int m = y.length();
    for (; i < n && i < m; i++) {
      Character c1 = x.charAt(i);
      Character c2 = y.charAt(i);
      if (Character.isLetterOrDigit(c1)) {
        map1[asciiValueOf(c1, Boolean.TRUE)]++;
      }
      if (Character.isLetterOrDigit(c2)) {
        map2[asciiValueOf(c2, Boolean.TRUE)]++;
      }
    }

    for (; i < n; i++) {
      Character c1 = x.charAt(i);
      if (Character.isLetterOrDigit(c1)) {
        map1[asciiValueOf(c1, Boolean.TRUE)]++;
      }
    }

    for (; i < m; i++) {
      Character c2 = y.charAt(i);
      if (Character.isLetterOrDigit(c2)) {
        map2[asciiValueOf(c2, Boolean.TRUE)]++;
      }
    }

    /**
     * Verify
     */
    for (int r = 0; r < 62; r++) {
      if (map1[r] == map2[r]) {
        continue;
      }
      return false;
    }
    return true;
  }
}













