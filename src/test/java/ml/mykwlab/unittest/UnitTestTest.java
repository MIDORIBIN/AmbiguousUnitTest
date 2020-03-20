package ml.mykwlab.unittest;

import ml.mykwlab.compile.CompileException;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class UnitTestTest {
    // 模範解答
    @Test
    public void runAmbiguousUnitTest() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertEquals(3, map.get(Status.SUCCESS).size());
        assertNull(map.get(Status.FAILURE));
        assertNull(map.get(Status.NOT_RUN));
    }

    // テンプレートじゃないやつ（ただのjavaファイル）
    @Test
    public void runAmbiguousUnitTestNotTemplate() throws IOException, CompileException {
        String template = readFile("RucksackTest.java");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertEquals(3, map.get(Status.SUCCESS).size());
        assertNull(map.get(Status.FAILURE));
        assertNull(map.get(Status.NOT_RUN));
    }

    // 依存ファイルなし
    @Test(expected = CompileException.class)
    public void runAmbiguousUnitTest1() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String rucksack = readFile("Rucksack.java");

        new DynamicTest(template, rucksack, Collections.emptyList());
    }

    // すべての項目はあるけど、すべて名前が間違ってる
    @Test
    public void runAmbiguousUnitTest2() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack2.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertEquals(3, map.get(Status.SUCCESS).size());
        assertNull(map.get(Status.FAILURE));
        assertNull(map.get(Status.NOT_RUN));
    }

    // 空ファイル、コンパイルが通る
    @Test(expected = CompileException.class)
    public void runAmbiguousUnitTest3() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack3.java");

        new DynamicTest(template, rucksack, Collections.singletonList(gum));
    }

    // 足りない項目がある
    @Test
    public void runAmbiguousUnitTest4() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack4.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertEquals(2, map.get(Status.SUCCESS).size());
        assertNull(map.get(Status.FAILURE));
        assertEquals(1, map.get(Status.NOT_RUN).size());
    }

    // メソッドなし
    // 実行するテストケースが一つもないとへんな感じのresultになる
    @Test
    public void runAmbiguousUnitTest5() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack5.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertNull(map.get(Status.SUCCESS));
        assertEquals(1, map.get(Status.FAILURE).size());
        assertEquals(3, map.get(Status.NOT_RUN).size());
    }

    // フィールド名が大きく間違ってる(arrayList -> list)
    // テストケースが0になる
    @Test
    public void runAmbiguousUnitTest6() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack6.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertNull(map.get(Status.SUCCESS));
        assertEquals(1, map.get(Status.FAILURE).size());
        assertEquals(3, map.get(Status.NOT_RUN).size());
    }

    // フィールド名が大きく間違ってる(arrayList -> list)
    // テストケースが0になる
    @Test
    public void runAmbiguousUnitTest7() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack7.java");

        DynamicTest dynamicTest = new DynamicTest(template, rucksack, Collections.singletonList(gum));
        Set<TestCaseResult> testCaseResultSet = dynamicTest.run();

        Map<Status, List<TestCaseResult>> map = testCaseResultSet.stream()
                .collect(Collectors.groupingBy(TestCaseResult::getStatus));

        assertEquals(1, map.get(Status.SUCCESS).size());
        assertEquals(1, map.get(Status.FAILURE).size());
        assertEquals(1, map.get(Status.NOT_RUN).size());
    }

    // debug
    private static String readFile(String fileName) throws IOException {
        Path file = Paths.get("src/main/resources/test/" + fileName);
        return Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
    }
}