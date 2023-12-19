package com.aggregated;

public class ExecutionResult {
    private final int fileProcessed;
    private final int fileCount;
    public ExecutionResult() {
        this.fileProcessed = 0;
        this.fileCount = 0;
    }
    public ExecutionResult(int processedFiles, int fileCount) {
        this.fileProcessed = processedFiles;
        this.fileCount = fileCount;
    }
    public int getFilesProcessed() {
        return fileProcessed;
    }

    public int getFileCount() {
        return fileCount;
    }
}














