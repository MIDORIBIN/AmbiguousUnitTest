package ml.mykwlab.creator;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ml.mykwlab.creator.TestCreateUtil.*;

public class DynamicTest {
    private int notRunTestCaseCount = 0;
    private String testCode;
    public DynamicTest(String template, Set<ClassStructure> classStructureSet) {
        this.testCode = templateToTestCode(template, classStructureSet);
    }

    private String templateToTestCode(String template, Set<ClassStructure> classStructureSet) {
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

    public String getTestCode() {
        return this.testCode;
    }

    public int getNotRunTestCaseCount() {
        return this.notRunTestCaseCount;
    }
}
