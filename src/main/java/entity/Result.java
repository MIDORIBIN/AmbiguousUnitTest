package entity;

public class Result {
    private boolean isSuccess;
    private String message;
    public Result(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean getIsSuccess() {
        return this.isSuccess;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return "Result{" +
                "isSuccess=" + isSuccess +
                ", message='" + message + '\'' +
                '}';
    }
}
