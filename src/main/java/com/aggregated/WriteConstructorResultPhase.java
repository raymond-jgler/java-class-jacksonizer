













package com.aggregated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WriteConstructorResultPhase extends BaseConstructorPhaseAlgorithm {
  public WriteConstructorResultPhase(RawClientRuleInput rawInput) {
    super(rawInput);
  }

  @Override
  public PhaseChainedResult execute(PhaseChainedResult previousInput) {
    BuildConstructorPhaseOutput prevOutput = (BuildConstructorPhaseOutput) previousInput;
    String                      ctorCode   = prevOutput.getRawValues().toString();
    prevOutput.reset();
    if (StringUtils.isEmpty(ctorCode) || "SKIP".equalsIgnoreCase(ctorCode)) {
      return new WriteConstructorResultPhaseOutput();
    }
    if (shouldSkipCurrentClass()) {
      return new WriteConstructorResultPhaseOutput();
    }
    try {
      Files.write(Paths.get(slashedFullName), ctorCode.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new WriteConstructorResultPhaseOutput();
  }

}
