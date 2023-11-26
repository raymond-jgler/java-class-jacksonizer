package com.aggregated.entry_point;

import com.aggregated.Driver;
import com.aggregated.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Objects;

public class InputEntry {
  private static final Logger      LOG             = LoggerFactory.getLogger(InputEntry.class);
  private final static String      INPUT_FILE_NAME        = "input.txt";
  private final static String      SKIP_INDICATOR  = "#";
  private static final String      BASE_PATH       = "src//main//java//";
  private static       boolean     isSinglePackage = false;
  public static void main(String[] args) {
    scanPackageOrSingleJava();
  }

  public static String resolveJavaFile(String inp) {
    int firstUpperCaseIdx = isPackage(inp);
    if (firstUpperCaseIdx == -1) {
      return inp;
    }
    String fullJavaName = inp.substring(0, firstUpperCaseIdx) + inp.substring(firstUpperCaseIdx) + ".java";
    return fullJavaName;
  }
  private static int isPackage(String inp) {
    for (int i = 0, n = inp.length(); i < n; i++) {
      if (Character.isUpperCase(inp.charAt(i))) {
        return i;
      }
    }
    return -1;
  }

  private static void scanPackageOrSingleJava() {
    /**
     * Build defined values
     */
    isSinglePackage = false;
    ClassLoader classLoader = InputEntry.class.getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(INPUT_FILE_NAME);
    if (Objects.isNull(inputStream)) {
      return;
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty() || line.contains(SKIP_INDICATOR)) {
          continue;
        }
        if (evalSinglePackage(line)) {
          isSinglePackage = true;
          line = resolveReplaces(line, ".*", "", "**", "", "*", "", " ", "");
          scanClassesInPackage(line);
        } else {
          line = resolveJavaFile(StringUtils.stripDoubleEndedNonAlphaNumeric(line));
          Driver.JacksonModeSingleJavaExecution(line);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String resolveReplaces(String orig, String... fromToPair) {
    final int PAIR_JUMP = 2;
    if (fromToPair.length % 2 != 0) {
      LOG.error("Not enough data to perform action");
    } ;
    for (int i = 0, n = fromToPair.length; i < n; i += PAIR_JUMP) {
      orig = orig.replace(fromToPair[i], fromToPair[i + 1]);
    }
    //fishy, ensure single-dotted only.
    return orig.replaceAll("\\.+", ".");
  }

  private static File resolveFolder(String packageName) {
    if (packageName.contains("src\\main\\java")) {
      return new File(packageName);
    }
    // convert a.b.c.** to a//b//c
    return new File(BASE_PATH + resolveReplaces(packageName, ".**", "", ".", "//"));
  }

  private static void scanClassesInPackage(String packageName) {
    LOG.info("==== scanning classes in package: " + packageName);
    String packagePath = resolveReplaces(packageName, ".**", "", ".", "//");
    Objects.requireNonNull(packageName);
    File folder = resolveFolder(packageName);
    if (Objects.isNull(folder.listFiles())) {
      return;
    }
    for (File each : Objects.requireNonNull(folder.listFiles())) {
      if (each.isDirectory()) {
        if (!isSinglePackage) {
          scanClassesInPackage(each.getPath());
        }
      }
    }
    String javaPackage = resolveReplaces(packagePath, "src\\main\\java\\", "", "\\", ".", "//", ".", " ", "");
    LOG.info("==== javaPackage: " + javaPackage.substring(0, javaPackage.length()));
    if (javaPackage.contains("*")) {
      javaPackage = javaPackage.substring(0, StringUtils.lastIndexOf(javaPackage, '.', null, null, null));
    }
    Driver.JacksonModePackageExecution(javaPackage);
  }
  private static boolean evalSinglePackage(String inp) {
    return StringUtils.countCharsFromEnd(inp, '*') == 1;
  }
}














