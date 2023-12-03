package com.aggregated;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringArsenal {
    private final Logger LOG = LoggerFactory.getLogger(StringArsenal.class);
    private static final StringArsenal INSTANCE = new StringArsenal();
    private static final String EMPTY = "";
    private static final String OPEN_PAREN = "(";
    private static final String CLOSE_PAREN = ")";
    private static final String AT = "@";
    private static final String COMMA = ",";
    private static final String DOT = ".";
    private static final String EQUAL = " = ";
    private static final String SPACE = " ";
    private static final char SEMICOLON = ';';
    private static final String SINGLE_BREAK = "\n";
    private final Map<String, String> rawImportToResovled = new HashMap<>();
    private String statefulData;
    /**
     * Output of a method returning a string.
     */
    private String resultantString;

    private StringArsenal() {
      this.statefulData = "";
        /**
         * If null, return the real internal stateful data,
         * otherwise return this one.
         */
      this.resultantString = null;
    }

    /**
     * By default, this overrides existing internal stateful string data.
     * @param data
     * @return
     */
    public StringArsenal with(String data) {
      this.statefulData = data;
      return this;
    }
    public static StringArsenal current() {
        return INSTANCE;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public static boolean isEmpty(String inp) {
        return Objects.isNull(inp) || current().with(inp).isEmpty();
    }

    public boolean isEmpty() {
        return Objects.isNull(this.statefulData) || this.statefulData.isEmpty();
    }

    public boolean isNoneBlank() {
        if (isEmpty()) {
            return false;
        }
        for (Character each : this.statefulData.toCharArray()) {
            if (Character.isLetterOrDigit(each)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAny(String... args) {
        for (String each : args) {
            if (this.statefulData.contains(each) || this.statefulData.contains(each) || each.contains(this.statefulData) || each.contains(this.statefulData)) {
                return true;
            }
        }
        return false;
    }

    public StringArsenal resolveReplaces(String... fromToPairs) {
        final int PAIR_JUMP = 2;
        if (fromToPairs.length % 2 != 0) {
            LOG.error("Not enough data to perform action");
        }
        for (int i = 0, n = fromToPairs.length; i < n; i += PAIR_JUMP) {
            this.statefulData = this.statefulData.replace(fromToPairs[i], fromToPairs[i + 1]);
        }
        //fishy, ensure single-dotted only.
        this.statefulData.replaceAll("\\.+", ".");
        return this;
    }

    public boolean endsWithAny(String... args) {
        for (String each : args) {
            if (this.statefulData.endsWith(each) || this.statefulData.endsWith(each + ".java")) {
                return true;
            }
        }
        return false;
    }

    public StringArsenal stripComments() {
        this.statefulData = this.statefulData.replaceAll("//.*", "");
        Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(this.statefulData);
        this.statefulData = matcher.replaceAll("");

        return this;
    }

    public StringArsenal appendIndentableBracketTo(String bracket, String indentVal) {
        if (this.statefulData.isEmpty() || this.statefulData.contains(String.valueOf(bracket))) {
            return this;
        }
        String res = this.statefulData;
        if (!bracket.equalsIgnoreCase(String.valueOf(res.charAt(res.length() - 1)))) {
            res += indentVal + bracket;
        }
        this.statefulData = res;
        return this;
    }

    public StringArsenal stripUntilDollarSign() {
        for (int i = 0, n = this.statefulData.length(); i < n; i++) {
            if (this.statefulData.charAt(i) == '$') {
                this.statefulData = this.statefulData.substring(0, i);
                return this;
            }
        }
        return this;
    }
    public StringArsenal stripUntilClassPath(Character... toKeep) {
        Set<Character> toKeeps = new HashSet<>(Arrays.asList(toKeep));
        StringBuilder sb = new StringBuilder();
        for (Character c : this.statefulData.toCharArray()) {
            if (Character.isLetterOrDigit(c) || toKeeps.contains(c)) {
                sb.append(c);
            }
        }
        this.statefulData = resolveReplaces(sb.toString(), "/", "").getInternal();
        return this;
    }

    public boolean isAllLowerCase() {
        for (Character c : this.statefulData.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }
    public String getInternal() {
        if (Objects.isNull(this.statefulData)) {
            return "";
        }
        return this.statefulData;
    }

    /**
     * Pincer-strip double-ended non alphanumeric chars from string,
     * until meets character / digit from both ends.
     *
     * 
     * @return
     */
    public StringArsenal stripDoubleEndedNonAlphaNumeric() {
        final int THRESHOLD = 200;
        final long start = System.currentTimeMillis();
        int left = 0, n = this.statefulData.length() - 1, right = n;
        while (left < right && left < this.statefulData.length() && !Character.isLetterOrDigit(this.statefulData.charAt(left))) {
            left++;
        }
        while (left < right && right > 0 && !Character.isLetterOrDigit(this.statefulData.charAt(right))) {
            right--;
        }
        //if unchanged.
        if (left >= right || (left == 0 && right == n)) {
            return this;
        }

        while (true) {
            if (System.currentTimeMillis() - start >= THRESHOLD) {
                break;
            }
            try {
                this.statefulData = this.statefulData.substring(left, right + 1);
                return this;
            } catch (Throwable t) {
                right -= 1;
            }
        }
        return this;
    }
    public int lastIndexOf(char x, Integer backwardFrom, Integer ordinalIndex, Boolean skipBreaker) {
        if (!this.statefulData.contains(String.valueOf(x))) {
            return -1;
        }
        if (Objects.isNull(skipBreaker)) {
            skipBreaker = true;
        }
        if (Objects.isNull(ordinalIndex)) {
            ordinalIndex = 1;
        }
        int n = this.statefulData.length() - 1;
        if (Objects.nonNull(backwardFrom)) {
            n = backwardFrom;
        }
        int matches = 0;
        int shrinkingI = n;
        for (int i = n; i >= 0; i--) {
            try {
                if (this.statefulData.charAt(i) == x) {
                    matches++;
                    if (ordinalIndex == -1) {
                        shrinkingI = i;
                        continue;
                    }
                    if (matches == ordinalIndex) {
                        return i;
                    }
                }
                if ((skipBreaker) && (this.statefulData.charAt(i) == '\r' || this.statefulData.charAt(i) == '\n')) {
                    break;
                }
            } catch (Throwable t) {
                i--;
            }
        }
        return shrinkingI;
    }

    public int countCharsFromEnd( char x) {
        int i = lastIndexOf(x, null, null, null);
        int count = 0;
        while (i >= 0 && this.statefulData.charAt(i) == x) {
            i--;
            count++;
        }
        return count;
    }
    public StringArsenal toNonAlphanumeric() {
        if (isEmpty()) {
            return this;
        }
        StringBuilder sb = new StringBuilder();
        StringBuilder res = new StringBuilder();
        for (Character c : this.statefulData.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
                continue;
            }
            if (sb.length() > 0) {
                res.append(sb.toString());
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) {
            res.append(sb.toString());
        }
        return this;
    }

    public List<String> toNonAlphaNumList() {
        if (isEmpty()) {
            return new ArrayList<>();
        }
        StringBuilder sb = new StringBuilder();
        List<String> res = new ArrayList<>();
        for (Character c : this.statefulData.toCharArray()) {
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

    /**
     * @return A spaced-string from list
     */
    public static String toStringFromList(List<String> inpList) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isEmpty(inpList)) {
            return "";
        }
        boolean isFirst = true;
        for (String each : inpList) {
            if (!isFirst) {
                stringBuilder.append(SPACE);
            }
            stringBuilder.append(each);
            isFirst = false;
        }
        return stringBuilder.toString();
    }

    public StringArsenal bulkCascadeRemoveSuffixedString(char suffix, Character... patternSplitterTeller) {
        final List<Character> teller = Arrays.asList(patternSplitterTeller);
        StringBuilder partitionCollector = new StringBuilder();
        StringBuilder removed = new StringBuilder();
        for (int i = 0, n = this.statefulData.length(); i < n; i++) {
            Character cur = this.statefulData.charAt(i);
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
        this.statefulData = finalSwticher;
        return this;
    }

    /**
     * Should be used when ony " ONE pattern range " is present.
     * This method will work for only 1 self-contained pattern.
     * not work for multiple pattern ranges.
     * <p>
     * A suffixed string is a pattern containing:
     * a word followed by a character.
     * for example, these are suffixed strings:
     * string =  java.util.List
     * suffixed strings : java., util.
     * non-suffixed : List
     *
     * @return a string having its suffixed ones removed.
     * <p>
     * this.data : java.util.List
     * output : List
     */
    private String cascadeRemoveSuffixedString(String inp, char suffix) {
        if (!this.statefulData.contains(String.valueOf(suffix))) {
            return this.statefulData;
        }
        int n = this.statefulData.length();
        int i = n - 1;
        int rightBound = StringArsenal.current().with(inp).lastIndexOf(suffix, i, 1, null);
        int leftBound = StringArsenal.current().with(inp).lastIndexOf(suffix, i, -1, null);
        /**
         * 2 cases for words preceding leftBound
         * _ a whole removable string
         * _ a partial removable string ( blocked by other words ).
         */
        i = leftBound - 1;
        for (; i >= 0 && Character.isLetterOrDigit(inp.charAt(i)); i--) {
        }
        leftBound = i;

        return StringArsenal.current().with(inp).ripRangeFromString(leftBound, rightBound);
    }

    public String ripRangeFromString(int exceptFrom, int exceptTo) {
        StringBuilder ripped = new StringBuilder();
        for (int i = 0, n = this.statefulData.length(); i < n; i++) {
            /**
             * Exclusive left bound
             * Inclusive right bound.
             */
            if (i > exceptFrom && i <= exceptTo) {
                continue;
            }
            ripped.append(this.statefulData.charAt(i));
        }
        return ripped.toString();
    }

    public boolean isPrefixedWith(String prefix, String content) {
        int n = content.length();
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

    public int firstIdxOfNonAlphanumeric() {
        for (int i = 0, n = this.statefulData.length(); i < n; i++) {
            if (Character.isLetterOrDigit(this.statefulData.charAt(i))) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public String buildAnnotationPackage(String unresolvedPackage, String annotation) {
        return (SPACE + unresolvedPackage + DOT + annotation + SEMICOLON).replace(AT, "");
    }

    /**
     * Will stop when reaching the last separator.
     * @param separator
     * @return
     */
    public StringArsenal getLastWord(String separator) {
        NullabilityUtils.isAllNonEmpty(true, this.statefulData, separator);
        StringBuilder rev = new StringBuilder();
        for (int n = this.statefulData.length(), i = n - 1; i >= 0; i--) {
            Character cur = this.statefulData.charAt(i);
            if (separator.equalsIgnoreCase(String.valueOf(cur))) {
                break;
            }
            rev.append(cur);
        }
        this.statefulData = rev.reverse().toString();
        return this;
    }

    /**
     * Separated by each dot,
     * ensure no more than 1 word contains >= 1 upper-case characters.
     *
     * 
     * @return
     */
    public StringArsenal correctifyImportString(Character sep) {
        if (isEmpty() || !this.statefulData.contains(String.valueOf(sep))) {
            return this;
        }
        if (!MapUtils.isEmpty(rawImportToResovled) && rawImportToResovled.containsKey(this.statefulData)) {
            this.statefulData = rawImportToResovled.get(this.statefulData);
            return this;
        }
        StringBuilder res = new StringBuilder();
        StringBuilder each = new StringBuilder();
        boolean isMetUppercase = false;
        for (int i = 0, n = this.statefulData.length(); i < n && !isMetUppercase; i++) {
            Character curr = this.statefulData.charAt(i);
            if (curr != sep) {
                each.append(curr);
                continue;
            }
            if (each.length() > 0 && !res.toString().contains(each)) {
                if (res.length() > 0) {
                    res.append(sep);
                }
                res.append(each);
                if (!isAllLowerCase()) {
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
        if (this.statefulData.charAt(0) == '.' || this.statefulData.charAt(this.statefulData.length() - 1) == '.') {
            final String rawText = stripDoubleEndedNonAlphaNumeric().getInternal();
            toPut = "java.util." + rawText;
        }
        rawImportToResovled.put(this.statefulData, toPut);
        this.statefulData = rawImportToResovled.get(this.statefulData);
        return this;
    }

    public boolean bidirectionalContains(String x, String y) {
        return x.contains(y) || y.contains(x);
    }

    public int firstIndexOf( char x, int start, boolean isBackward) {
        if (isBackward) {
            for (int i = start; i >= 0; i--) {
                if (x == this.statefulData.charAt(i)) {
                    return i;
                }
            }
        } else {
            for (int i = start, n = this.statefulData.length(); i < n; i++) {
                if (x == this.statefulData.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Space is handled by the caller.
     *
     * @param key
     * @param value
     * @param operator
     * @return
     */
    public String formKeyValuePair(String key, String value, String operator) {
        NullabilityUtils.isAllNonEmpty(true, key, value, operator);
        return key + operator + value;
    }

    public StringArsenal concatenateWith(String ...inp) {
        for (String each : inp) {
            this.statefulData += each;
        }
        return this;
    }
    public StringArsenal findPrependablePieceFrom(int backwardFrom, Character breakingChar, boolean isSkipSpace) {
        if (Objects.isNull(breakingChar)) {
            breakingChar = '\r';
        }
        StringBuilder rev = new StringBuilder();
        for (int i = backwardFrom; i >= 0; i--) {
            Character c = this.statefulData.charAt(i);
            if (String.valueOf(this.statefulData.charAt(i)).equalsIgnoreCase(SINGLE_BREAK) || this.statefulData.charAt(i) == breakingChar) {
                break;
            }
            if (isSkipSpace && !Character.isLetterOrDigit(c)) {
                continue;
            }
            rev.append(c);
        }
        this.statefulData = rev.reverse().toString();
        return this;
    }

    /**
     * @param c          must be alphanumeric.
     * @param isRebounce use 0 as index.
     * @return
     */
    public int asciiValueOf(char c, boolean isRebounce) {
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
     * @return
     */
    public boolean isAnagram(String that) {
        /**
         * Set A = {Aa-Zz + 0 -> 9} -> nO of chars =  62
         */
        int[] map1 = new int[62];
        int[] map2 = new int[62];
        Arrays.fill(map1, 0);
        Arrays.fill(map2, 0);
        int i = 0;
        int n = this.statefulData.length();
        int m = that.length();
        for (; i < n && i < m; i++) {
            Character c1 = this.statefulData.charAt(i);
            Character c2 = that.charAt(i);
            if (Character.isLetterOrDigit(c1)) {
                map1[asciiValueOf(c1, Boolean.TRUE)]++;
            }
            if (Character.isLetterOrDigit(c2)) {
                map2[asciiValueOf(c2, Boolean.TRUE)]++;
            }
        }

        for (; i < n; i++) {
            Character c1 = this.statefulData.charAt(i);
            if (Character.isLetterOrDigit(c1)) {
                map1[asciiValueOf(c1, Boolean.TRUE)]++;
            }
        }

        for (; i < m; i++) {
            Character c2 = that.charAt(i);
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













