package ml.mykwlab.unittest;

import ml.mykwlab.compile.CompileException;
import ml.mykwlab.template.ClassStructure;
import org.apache.commons.text.StringSubstitutor;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static ml.mykwlab.compile.Compile.compile;
import static ml.mykwlab.template.TestCreateUtil.*;
import static ml.mykwlab.unittest.UnitTest.runUnitTest;

public class DynamicTest {
    private String testCode;
    private List<String> codeList = new ArrayList<>();
    private Set<TestCaseResult> testCaseResultSet = new HashSet<>();

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
            String methodName = templateBlock.split("\\svoid\\s")[1].split("\\s", 2)[0].replace("()", "");
            this.testCaseResultSet.add(new TestCaseResult(methodName, Status.NOT_RUN, ""));
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

    public Set<TestCaseResult> run() throws CompileException {
        Class<?> testClass = compile(this.testCode, codeList).getTargetClass();
        Result result = runUnitTest(testClass);

        this.testCaseResultSet.addAll(createFailureTestCase(result.getFailures()));

        this.testCaseResultSet.addAll(createSuccessTestCase(result.getFailures(), testClass));

        return this.testCaseResultSet;
    }

    private static Set<TestCaseResult> createFailureTestCase(List<Failure> failureList) {
        return failureList.stream()
                .map(failure -> new TestCaseResult(failure.getDescription().getMethodName(), Status.FAILURE, failure.getMessage()))
                .collect(Collectors.toSet());
    }

    private static Set<TestCaseResult> createSuccessTestCase(List<Failure> failureList, Class<?> testClass) {
        List<String> failureMethodNameList = failureList.stream()
                .map(failure -> failure.getDescription().getMethodName())
                .collect(Collectors.toList());

        return Arrays.stream(testClass.getDeclaredMethods())
                .filter(DynamicTest::isTestMethod)
                .filter(method -> !failureMethodNameList.contains(method.getName()))
                .map(Method::getName)
                .map(methodName -> new TestCaseResult(methodName, Status.SUCCESS, ""))
                .collect(Collectors.toSet());
    }

        private static boolean isTestMethod(Method method) {
        return Arrays.stream(method.getDeclaredAnnotations())
                .map(Annotation::annotationType)
                .map(Class::toString)
                .anyMatch(typeName -> typeName.equals("interface org.junit.Test"));
    }

//    public String getTestCode() {
//        return testCode;
//    }
}
