package com.aggregated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ExecutionRule {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionRule.class);
    private static Map<String, ExecutionRule> MEM_CACHED = new HashMap();
    private final String code;
    private final String description;
    /**
     * Default : Won't skip any, all ( inner static included ) classes are executed.
     */
    public static final ExecutionRule ALL = create("default", "Default Execution Rule, execute all encountered classes, including inner");
    /**
     * //TODO not supported yet.
     */
    public static final ExecutionRule SKIP_INNER_CLASSES = create("skip_inner_classes", "Skip All Encountered Inner Classes ");


    @JsonCreator
    private ExecutionRule (@JsonProperty("code") String code,
                           @JsonProperty("description") String description) {

        this.code = code;
        this.description = description;
    }

    public static ExecutionRule getFromCache(String code) {
        Objects.requireNonNull(code);
        if (MEM_CACHED.isEmpty()) {
            LOG.warn("memory storage is empty, null value is returned");
            return null;
        }
        if (!MEM_CACHED.containsKey(code)) {
            LOG.warn("Code = " + code + " not found " + ", null value is returned");
            return null;
        }
        return MEM_CACHED.getOrDefault(code, null);
    }

    private static ExecutionRule create(String code, String description) {
        NullabilityUtils.requireAllNonNull(true, code, description);
        ExecutionRule executionRule = new ExecutionRule(code, description);
        MEM_CACHED.putIfAbsent(code, executionRule);
        return MEM_CACHED.getOrDefault(code, null);
    }
}













