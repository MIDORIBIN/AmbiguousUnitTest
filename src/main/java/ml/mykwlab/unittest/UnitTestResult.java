package ml.mykwlab.unittest;

import org.junit.runner.Result;

public class UnitTestResult {
    private int successCount;
    private int failureCount;
    private int notRunCount;

    public UnitTestResult (Result result, int notRunCount) {
        this.successCount = result.getRunCount() - result.getFailureCount();
        this.failureCount = result.getFailureCount();
        this.notRunCount = notRunCount;
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public int getFailureCount() {
        return this.failureCount;
    }

    public int getNotRunCount() {
        return this.notRunCount;
    }
}
