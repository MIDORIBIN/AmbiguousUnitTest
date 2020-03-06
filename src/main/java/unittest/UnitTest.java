package unittest;

import compile.CompileClasses;
import compile.CompileException;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static compile.Compile.compile;
import static creator.Creator.templateToJava;

public class UnitTest {

    public static void main(String[] args) throws IOException {
        String template = readFile("perfectSample.template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack2.java");

        Result result;
        try {
            result = runUnitTest(template, rucksack, Collections.singletonList(gum));
        } catch (CompileException e) {
            e.printStackTrace();
            result = new Result(false, e.getMessage());
        }
        System.out.println(result);
    }

    // debug
    private static String readFile(String fileName) throws IOException {
        Path file = Paths.get("src/main/resources/test/" + fileName);
        return Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
    }

    private static Result runUnitTest(String template, String target, List<String> others) throws CompileException {
        CompileClasses compileClasses = compile(target, others);

        String test = templateToJava(template, compileClasses);

        List<String> javaCodeList = new ArrayList<>();
        javaCodeList.add(target);
        javaCodeList.addAll(others);

        Class<?> testClass = compile(test, javaCodeList).getTargetClass();

        return runUnitTest(testClass);
    }

    /**
     * ユニットテスト
     * 標準出力の問題からこのメソッドは同時に呼ばれないようになってる
     * @param testClass junitのクラスクラス
     * @return ユニットテストの評価結果
     */
    private synchronized static Result runUnitTest(Class<?> testClass) {
        PrintStream defaultPrintStream = System.out;

        JUnitCore jUnitCore = new JUnitCore();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
        PrintStream resultStream = new PrintStream(resultBytes);
        jUnitCore.addListener(new TextListener(resultStream));
        org.junit.runner.Result result = jUnitCore.run(testClass);

        System.setOut(defaultPrintStream);

        for (Failure failure : result.getFailures()) {
            System.out.println(failure.getDescription());
            System.out.println(failure.getMessage());
            System.out.println(failure.getTestHeader());
            System.out.println(failure.getTrace());
            System.out.println(failure.getTrimmedTrace());
        }

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
