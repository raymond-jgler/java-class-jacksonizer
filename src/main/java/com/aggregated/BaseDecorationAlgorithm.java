package com.aggregated;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseDecorationAlgorithm implements DecorationAlgorithm {
  private static final Logger                              LOG                 =
          LoggerFactory.getLogger(BaseDecorationAlgorithm.class);
  protected final      InputReceiver                       inputReceiver;
  protected final      List<BaseConstructorPhaseAlgorithm> algorithmPhases;
  protected final      ExecutionRule                       rule;
  protected final      RawClientRuleInput                  rawInput;
  protected final      DecorationStrategyMode              mode;
  protected final      List<File>                          processibleFileList = new ArrayList();
  protected            int                                 filesProcessed;
  protected            int                                 fileCount;
  @JsonCreator
  protected BaseDecorationAlgorithm(RawClientRuleInput rawInput, int fileCount, List algorithmPhases, InputReceiver inputReceiver, int filesProcessed, ExecutionRule rule,DecorationStrategyMode mode) {

    this.rawInput = rawInput;
    this.fileCount = fileCount;
    this.algorithmPhases = algorithmPhases;
    this.inputReceiver = inputReceiver;
    this.filesProcessed = filesProcessed;
    this.rule = rule;
    this.mode = mode;
  }
  protected BaseDecorationAlgorithm(InputReceiver inputReceiver,
                                    ExecutionRule rule,
                                    RawClientRuleInput rawInput,
                                    DecorationStrategyMode mode) {
    this.inputReceiver   = inputReceiver;
    this.rule            = rule;
    this.rawInput        = rawInput;
    this.algorithmPhases = AlgorithmPhaseBuilder.buildConcretePhases(rawInput, mode);
    this.mode            = mode;
    /**
     * build processibleFileList
     */
    beginTakeFiles();
  }

  protected abstract boolean isValidStrategy();

  protected abstract void executePhases(PhaseChainedResult input);

  //TODO shuda let file utils do this.
  private void beginTakeFiles() {
    if (StringArsenal.current().with(this.rawInput.getSingleJavaFileName()).isNotEmpty()) {
      this.processibleFileList.add(FileUtils.makeFolderOrFile(this.rawInput.getSingleJavaFileName()));
      return;
    }
    File fileOrFolder = FileUtils.makeFolderOrFile(inputReceiver.getValues());
    if (Objects.isNull(fileOrFolder) || Objects.isNull(fileOrFolder.listFiles())) {
      return;
    }
    takeRootOrAllFiles(fileOrFolder);
  }

  //TODO shuda let file utils do this.
  protected void takeRootOrAllFiles(File files) {
    if (Objects.isNull(files) || Objects.isNull(files.listFiles())) {
      final String errorMsg = "Can't proceed with corrupt files..";
      LOG.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
    for (File file : files.listFiles()) {
      if (file.isDirectory()) {
        if (rawInput.isSubPackages()) {
          takeRootOrAllFiles(file);
        }
      } else if (file.exists()) {
        processibleFileList.add(file);
      }
    }
    fileCount = processibleFileList.size();
  }
}















