package com.aggregated;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IndentationUtils {
  private static final Logger              LOG             = LoggerFactory.getLogger(IndentationUtils.class);
  private static       AtomicInteger       currentIndentValue;
  public static final  String              LINE_BREAK      = "\n";
  public static final  String              TAB             = "\t";
  public static final  String              OUTER_BLOCK_TAB = "OUTER_BLOCK_TAB";
  public static final  String              INNER_BLOCK_TAB = "INNER_BLOCK_TAB";
  public static final  int                 INITIAL_VALUE   = 0;
  private static       Map<String, String> outerAndInnerTabs;

  private IndentationUtils() {
    currentIndentValue = new AtomicInteger(INITIAL_VALUE);
  }

  private static void incrementIndent() {
    if (Objects.isNull(currentIndentValue)) {
      currentIndentValue = new AtomicInteger(INITIAL_VALUE);
    }
    currentIndentValue.getAndIncrement();
  }

  private static void decrementIndent() {
    if (Objects.isNull(currentIndentValue)) {
      LOG.error("Indent value is null");
      return;
    }
    currentIndentValue.decrementAndGet();
  }

  public static void resetIndent() {
    if (Objects.isNull(currentIndentValue)) {
      currentIndentValue = new AtomicInteger(0);
    } else {
      currentIndentValue.set(0);
    }
  }
  public static String genCharsWithLen(char x, int length) {
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < length - 1; i++) {
      res.append(x);
    }
    return res.toString();
  }

  /**
   * Build with default ( = 1)
   * or given initialIndentVal.
   */
  public static Map<String, String> build(Optional<Integer> indentVal) {
    if (!indentVal.isPresent()) {
      indentVal = Optional.of(Objects.isNull(currentIndentValue) ? 1 : currentIndentValue.get());
    }
    currentIndentValue.set(indentVal.get());
    if (MapUtils.isEmpty(outerAndInnerTabs)) {
      outerAndInnerTabs = new ConcurrentHashMap<>();
    } else {
      outerAndInnerTabs.clear();
    }
    final int STARTER = 1;
    int       i       = 1;
    do {
      if (i == STARTER) {
        outerAndInnerTabs.put(OUTER_BLOCK_TAB, outerAndInnerTabs.getOrDefault(OUTER_BLOCK_TAB, TAB));
      } else {
        outerAndInnerTabs.put(OUTER_BLOCK_TAB, outerAndInnerTabs.getOrDefault(OUTER_BLOCK_TAB, TAB) + TAB);
      }
      outerAndInnerTabs.put(INNER_BLOCK_TAB, outerAndInnerTabs.getOrDefault(INNER_BLOCK_TAB, TAB) + TAB);
      i++;
    } while (i <= currentIndentValue.get());

    return outerAndInnerTabs;
  }

  public static void incrementTabMap() {
    if (MapUtils.isEmpty(outerAndInnerTabs)) {
      outerAndInnerTabs = new ConcurrentHashMap<>();
    }
    incrementIndent();
    /**
     * Outer - 1 tab
     * Inner - 2 tabs
     */
    if (currentIndentValue.get() == 1) {
      outerAndInnerTabs.put(OUTER_BLOCK_TAB, TAB);
    } else {
      outerAndInnerTabs.put(OUTER_BLOCK_TAB, outerAndInnerTabs.getOrDefault(OUTER_BLOCK_TAB, TAB) + TAB);
    }
    outerAndInnerTabs.put(INNER_BLOCK_TAB, outerAndInnerTabs.getOrDefault(INNER_BLOCK_TAB, TAB) + TAB);
  }
  private static void resetTabMap() {
    resetIndent();
    outerAndInnerTabs = new ConcurrentHashMap<>();
  }

  public static void decrementTabMap() {
    if (MapUtils.isEmpty(outerAndInnerTabs)) {
      LOG.error("Tab map's value is null");
      return;
    }
    decrementIndent();
    outerAndInnerTabs.put(OUTER_BLOCK_TAB, stripLastVal(outerAndInnerTabs.get(OUTER_BLOCK_TAB)));
    outerAndInnerTabs.put(INNER_BLOCK_TAB, stripLastVal(outerAndInnerTabs.get(INNER_BLOCK_TAB)));
  }

  /**
   * Build exactly up to n - 1 values.
   *
   * @param val
   * @return
   */
  private static String stripLastVal(String val) {
    StringBuilder penUltiVal = new StringBuilder();
    for (int i = 0, n = val.length(); i < n - 1; i++) {
      String currTab = String.valueOf(val.charAt(i));
      if (!TAB.equalsIgnoreCase(currTab)) {
        final String errorMsg = "Invalid tab value";
        LOG.error(errorMsg);
        throw new RuntimeException(errorMsg);
      }
      penUltiVal.append(currTab);
    }
    return penUltiVal.toString();
  }

  public static Map<String, String> getResult() {
    return MapUtils.isEmpty(outerAndInnerTabs) ? null : outerAndInnerTabs;
  }

  public static String get(String tabKey) {
    if (MapUtils.isEmpty(outerAndInnerTabs)) {
      resetTabMap();
    }
    if (!outerAndInnerTabs.containsKey(tabKey)) {
      throw new RuntimeException("Tabkey is invalid!");
    }
    //has to be fishy tho
    if (tabKey.equalsIgnoreCase(INNER_BLOCK_TAB)) {
      rebalanceBasedOnOuterTab();
    }
    return outerAndInnerTabs.get(tabKey);
  }

  private static void rebalanceBasedOnOuterTab() {
    int outerTabLen = outerAndInnerTabs.get(OUTER_BLOCK_TAB).length();
    StringBuilder innerTabs = new StringBuilder();
    for (int i = 0; i <= outerTabLen; i++) {
      innerTabs.append(TAB);
    }
    outerAndInnerTabs.put(INNER_BLOCK_TAB, innerTabs.toString());
  }
}



