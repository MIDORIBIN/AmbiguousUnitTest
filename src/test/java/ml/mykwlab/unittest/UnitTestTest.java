package ml.mykwlab.unittest;

import ml.mykwlab.compile.CompileException;
import org.junit.Test;
import org.junit.runner.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


public class UnitTestTest {
    // 模範解答
    @Test
    public void runAmbiguousUnitTest() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack.java");

        Result result = UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));

        assertEquals(3, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    // 依存ファイルなし
    @Test(expected = CompileException.class)
    public void runAmbiguousUnitTest1() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String rucksack = readFile("Rucksack.java");

        UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.emptyList());
    }

    // すべての項目はあるけど、すべて名前が間違ってる
    @Test
    public void runAmbiguousUnitTest2() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack2.java");

        Result result = UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));
        assertEquals(3, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    // 空ファイル、コンパイルが通る
    @Test(expected = CompileException.class)
    public void runAmbiguousUnitTest3() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack3.java");

        UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));
    }

    // 足りない項目がある
    @Test
    public void runAmbiguousUnitTest4() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack4.java");

        Result result = UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));
        assertEquals(2, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }

    // メソッドなし
    @Test
    public void runAmbiguousUnitTest5() throws IOException, CompileException {
        String template = readFile("RucksackTest.java_template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack5.java");

        Result result = UnitTest.runAmbiguousUnitTest(template, rucksack, Collections.singletonList(gum));
        System.out.println(result.getFailures());
        assertEquals(1, result.getRunCount());
        assertEquals(0, result.getFailureCount());
    }


    // debug
    private static String readFile(String fileName) throws IOException {
        Path file = Paths.get("src/main/resources/test/" + fileName);
        return Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
    }
}