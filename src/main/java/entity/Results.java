package entity;

public class Results {
    private Result compile;
    private Result unitTest;

    public Results(Result compile, Result unitTest) {
        this.compile = compile;
        this.unitTest = unitTest;
    }

    public Result getCompile() {
        return this.compile;
    }

    public Result getUnitTest() {
        return this.unitTest;
    }
}
