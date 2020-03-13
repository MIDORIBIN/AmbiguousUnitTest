package ml.mykwlab.creator;

import ml.mykwlab.compile.CompileException;
import ml.mykwlab.unittest.UnitTestResult;
import org.apache.commons.text.StringSubstitutor;
import org.junit.runner.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ml.mykwlab.compile.Compile.compile;
import static ml.mykwlab.creator.TestCreateUtil.*;
import static ml.mykwlab.unittest.UnitTest.runUnitTest;

public class DynamicTest {
    private int notRunTestCaseCount = 0;
    private String testCode;
    private List<String> codeList = new ArrayList<>();
    public DynamicTest(String template, String target, List<String> others) throws CompileException {
        this.codeList.add(target);
        this.codeList.addAll(others);
        this.testCode = templateToTestCode(template, target, others);
    }

    private String templateToTestCode(String template, String target, List<String> others) throws CompileException {
        Set<ClassStructure> classStructureSet = createClassStructureSet(target, others);
        return splitTemplate(template).stream()
                .map(templateBlock -> conversionJava(templateBlock, classStructureSet))
                .collect(Collectors.joining());
    }

    private String conversionJava(String templateBlock, Set<ClassStructure> classStructureSet) {
        // template 以外
        if (!isTemplateMethod(templateBlock)) {
            return templateBlock;
        }
        Map<String, String> ambiguousMap = createAmbiguousMap(templateBlock, classStructureSet);
        // template で足りないメソッドがある
        if (!isAllImplementation(ambiguousMap)) {
            this.notRunTestCaseCount++;
            return "";
        }
        // 完全なテンプレート
        return deploymentTemplate(templateBlock, ambiguousMap);
    }

    private static boolean isTemplateMethod(String block) {
        return block.contains("${");
    }

    private static boolean isAllImplementation(Map<String, String> ambiguousMap) {
        return !ambiguousMap.containsValue("");
    }

    private static String deploymentTemplate(String template, Map<String, String> ambiguousMap) {
        return new StringSubstitutor(ambiguousMap).replace(template);
    }

    public UnitTestResult run() throws CompileException {
        Class<?> testClass = compile(this.testCode, codeList).getTargetClass();
        Result result = runUnitTest(testClass);
        return new UnitTestResult(result, this.notRunTestCaseCount);
    }

//    public String getTestCode() {
//        return testCode;
//    }
}
