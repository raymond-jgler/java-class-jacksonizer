package com.aggregated;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IndentationUtils {
  private static final Logger              LOG             = LoggerFactory.getLogger(IndentationUtils.class);
  private static int currentIndentValue;
  public static final  String              LINE_BREAK      = "\n";
  public static final  String              TAB             = "\t";
  public static final  String              OUTER_BLOCK_TAB = "OUTER_BLOCK_TAB";
  public static final  String              INNER_BLOCK_TAB = "INNER_BLOCK_TAB";
  public static final  int                 INITIAL_VALUE   = 0;
  private static       Map<String, String> outerAndInnerTabs;

  private IndentationUtils() {
    currentIndentValue = INITIAL_VALUE;
  }

  private static void incrementIndent() {
    currentIndentValue++;
  }

  private static void decrementIndent() {
    --currentIndentValue;
  }

  public static void resetIndent() {
    currentIndentValue = 0;
  }
  public static String genCharsWithLen(char x, int length) {
    final int availThreads = Runtime.getRuntime().availableProcessors();
    int possibleThreads = length / availThreads;
    if (possibleThreads < 2) {
      char[] chars = new char[length];
      int idx = 0;
      for (int i = 0; i < length; i++) {
        chars[idx++] = x;
      }
      return new String(chars);
    } else {
      Thread[] threads = new Thread[availThreads];
      int threadIdx = 0;
      AtomicInteger decreaser = new AtomicInteger(availThreads);
      AtomicInteger increaser = new AtomicInteger(0);
      AtomicReference<StringBuffer> stringBufferAtomicReference = new AtomicReference<>(new StringBuffer());
      for (int i = 0; i < threads.length; i++) {
        AtomicInteger idx = new AtomicInteger(increaser.get());
        AtomicInteger atomicLen = new AtomicInteger(length / decreaser.get());
        Runnable runnable = (() -> {
          for (; idx.get() < atomicLen.get() - 1; idx.getAndIncrement()) {
            stringBufferAtomicReference.get().append(x);
          }
        });
        if (idx.get() >= atomicLen.get()) {
          break;
        }
        threads[threadIdx++] = new Thread(runnable);
        increaser.set(atomicLen.get() + 1);
        decreaser.getAndDecrement();
      }
      ThreadUtils.executeAndJoinAll(threads);
      return stringBufferAtomicReference.get().toString();
    }
  }

  /**
   * Build with default ( = 1)
   * or given initialIndentVal.
   */
  public static Map<String, String> build(int indentVal) {
    currentIndentValue = indentVal;
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
    } while (i <= currentIndentValue);

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
    if (currentIndentValue == 1) {
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



