package com.aggregated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefaultConstructorDecorator extends BaseDecorationAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultConstructorDecorator.class);

    @JsonCreator
    public DefaultConstructorDecorator(@JsonProperty("inputReceiver") InputReceiver inputReceiver,
                                       @JsonProperty("rule") ExecutionRule rule,
                                       @JsonProperty("rawInput") RawClientRuleInput rawInput,
                                       @JsonProperty("mode") DecorationStrategyMode mode) {

        super(inputReceiver, rule, rawInput, mode);
    }
    @Override
    public ExecutionResult execute() {
        if (!isValidStrategy()) {
            return null;
        }
        IndentationUtils.resetIndent();
        IndentationUtils.incrementTabMap();
        for (File each : processibleFileList) {
            Class clazz = ReflectionUtils.getClass(each.getPath());
            /**
             * For class's pretty indentation
             */
            processTrusted(clazz, Boolean.FALSE);
            if (ReflectionUtils.hasInnerClasses(clazz)) {
                recursivelyProcessTrustedInnerClasses(Arrays.asList(clazz.getDeclaredClasses()));
            } else {
                IndentationUtils.resetIndent();
            }
        }
        return new ExecutionResult(filesProcessed, fileCount);
    }
    @Override
    public void executePhases(PhaseChainedResult input) {
        PhaseChainedResult runnerOutput = null;
        for (BaseConstructorPhaseAlgorithm phase : algorithmPhases) {
            runnerOutput = phase.execute(input);
            input = runnerOutput;
            if (runnerOutput.failVerify()) {
                LOG.info("May need to skip this class");
                break;
            }
        }
        filesProcessed++;
    }
    @Override
    public boolean isValidStrategy() {
        if (!DecorationStrategyMode.DEFAULT.equals(mode) && !DecorationStrategyMode.DEFAULT_CONSTRUCTOR.equals(mode)) {
            throw new RuntimeException("Strategy mode is invalid");
        }
        return true;
    }
    private void processTrusted(Class innerClazz, boolean isInner) {
        if (isInner) {
            IndentationUtils.incrementTabMap();
            BaseConstructorPhaseAlgorithm.updateWith(innerClazz, false);
        } else {
            BaseConstructorPhaseAlgorithm.beginWith(innerClazz);
        }
        executePhases(InitialDummyPhase.emptyInstance());
    }
    /**
     * Process adding rule-based constructors
     * to inner classes recursively
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
}













