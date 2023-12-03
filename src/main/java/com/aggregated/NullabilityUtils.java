package com.aggregated;

import java.util.Objects;

public class NullabilityUtils {
  public static boolean requireAllNonNull(boolean isExit, Object... params) {
    if (Objects.isNull(params)) {
      return false;
    }
    for (Object each : params) {
      try {
        if (Objects.isNull(each)) {
          return false;
        }
        Objects.requireNonNull(each);
      } catch (Throwable t) {
        if (isExit) {
          throw new RuntimeException(t.getMessage());
        }
        return false;
      }
    }
    return true;
  }

  public static boolean isAllNonEmpty(boolean isExit, String... params) {

    if (Objects.isNull(params) || "null".equalsIgnoreCase(params[0])) {
      return false;
    }
    for (String each : params) {
      if (StringArsenal.isEmpty(each)) {
        if (isExit) {
          throw new RuntimeException(each + " is null/empty");
        }
      }
    }
    return true;
  }

  public static boolean isAnyNullIn(Object... params) {
    for (Object object : params) {
      if (Objects.isNull(object)) {
        return true;
      }
    }
    return false;
  }
}














