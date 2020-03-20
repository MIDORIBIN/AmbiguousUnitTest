package ml.mykwlab.unittest;

public class TestCaseResult {
    private String testCaseName;
    private Status status;
    private String message;

    public TestCaseResult(String testCaseName, Status status, String message) {
        this.testCaseName = testCaseName;
        this.status = status;
        this.message = message;
    }

    public String getTestCaseName() {
        return this.testCaseName;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "TestCaseResult{" +
                "testCaseName='" + testCaseName + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
