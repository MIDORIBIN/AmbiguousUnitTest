package ml.mykwlab.unittest;

import ml.mykwlab.compile.CompileClasses;
import ml.mykwlab.compile.CompileException;
import ml.mykwlab.creator.DynamicTest;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

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

import static ml.mykwlab.compile.Compile.compile;
import static ml.mykwlab.creator.TestCreateUtil.createClassStructureSet;

public class UnitTest {

    public static void main(String[] args) throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack2.java");

        Result result = runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));
        System.out.println(result);
    }

    // debug
    private static String readFile(String fileName) throws IOException {
        Path file = Paths.get("src/main/resources/test/" + fileName);
        return Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
    }

    public static Result runAmbiguousUnitTest(String template, String target, List<String> others) throws CompileException {
        CompileClasses compileClasses = compile(target, others);

        DynamicTest dynamicTest = new DynamicTest(template, createClassStructureSet(compileClasses));
        String test = dynamicTest.getTestCode();

        List<String> javaCodeList = new ArrayList<>();
        javaCodeList.add(target);
        javaCodeList.addAll(others);

        Class<?> testClass = compile(test, javaCodeList).getTargetClass();

        return runAmbiguousUnitTest(testClass);
    }

    /**
     * ユニットテスト
     * 標準出力の問題からこのメソッドは同時に呼ばれないようになってる
     * @param testClass junitのクラスクラス
     * @return ユニットテストの評価結果
     */
    private synchronized static Result runAmbiguousUnitTest(Class<?> testClass) {
        PrintStream defaultPrintStream = System.out;

        JUnitCore jUnitCore = new JUnitCore();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
        PrintStream resultStream = new PrintStream(resultBytes);
        jUnitCore.addListener(new TextListener(resultStream));
        Result result = jUnitCore.run(testClass);

        System.setOut(defaultPrintStream);

        // 無限ループ対策
        stopTestThread();

        return result;
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
