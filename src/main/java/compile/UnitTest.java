package compile;

import entity.Result;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class UnitTest {

    /**
     * ユニットテスト
     * 標準出力の問題からこのメソッドは同時に呼ばれないようになってる
     * @param testClass junitのクラスクラス
     * @return ユニットテストの評価結果
     */
    public synchronized static Result runUnitTest(Class<?> testClass) {
        PrintStream defaultPrintStream = System.out;

        JUnitCore jUnitCore = new JUnitCore();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
        PrintStream resultStream = new PrintStream(resultBytes);
        jUnitCore.addListener(new TextListener(resultStream));
        org.junit.runner.Result result = jUnitCore.run(testClass);

        System.setOut(defaultPrintStream);

        // Tests run: 3,  Failures: 3
        String message = "Tests " +
                "run: " + result.getRunCount() + ", " +
                "Failures: " + result.getFailureCount();

        // 無限ループ対策
        stopTestThread();

        return new Result(result.wasSuccessful(), message);
    }

    /**
     * 全てのスレッドを探索して、Time-limited testの名前のスレッドを強制終了
     */
    @SuppressWarnings("deprecation")
    private static void stopTestThread() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread: threadSet) {
            if (thread.getName().equals("Time-limited test")) {
                thread.stop();
            }
        }
    }
}
