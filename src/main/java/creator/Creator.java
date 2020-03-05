package creator;

import compile.ClassFileManager;
import compile.JavaCode;
import org.apache.commons.text.StringSubstitutor;
import org.apache.lucene.search.spell.LevensteinDistance;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static compile.Compile.compile;
import static compile.UnitTest.runUnitTest;


public class Creator {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        String template = readFile("RucksackTest.template");
        String gum = readFile("Gum.java");
        String rucksack = readFile("Rucksack2.java");
//        func(template, rucksack, Collections.singletonList(gum));


        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager manager = new ClassFileManager(compiler);
    }

    public static void func(String template, String target, List<String> others) throws ClassNotFoundException {
        JavaCode targetCode = createJavaCodeFromCode(target);
        List<JavaCode> otherCodes = others.stream()
                .map(Creator::createJavaCodeFromCode)
                .collect(Collectors.toList());

        Class<?> testTarget = createTestTargetClass(targetCode, otherCodes);

        String test = templateToJava(template, testTarget);
        JavaCode testCode = createJavaCodeFromCode(test);

        List<JavaCode> javaCodeList = new ArrayList<>();
        javaCodeList.add(targetCode);
        javaCodeList.addAll(otherCodes);

//        runUnitTest()


//        Results results = compileAndRun(testCode, javaCodeList);
//        System.out.println(results.getUnitTest().getIsSuccess());
//        System.out.println(results.getUnitTest().getMessage());
    }

    public static Class<?> createTestTargetClass(JavaCode testTarget, List<JavaCode> others) throws ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager manager = new ClassFileManager(compiler);

        compile(testTarget, others, manager);

        return manager.getClassLoader(null).loadClass(testTarget.getName());

    }

    public static JavaCode createJavaCodeFromCode(String code) {
        String className = extraClassNameFromCode(code);
        return new JavaCode(className, code);
    }

    /**
     * クラス名をソースコード上から取得
     * 取得の仕方が甘い
     * コメントとかを挿入されたら厳しい
     * @param code
     */
    public static String extraClassNameFromCode(String code) {
        Pattern p = Pattern.compile("class\\s+([a-zA-Z]+)");
        Matcher m = p.matcher(code);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public static String templateToJava(String template, Class<?> targetClass) throws ClassNotFoundException {
        Map<String, String> map = createMap(template, targetClass);
        return new StringSubstitutor(map).replace(template);
    }

    public static Map<String, String> createMap(String template, Class<?> targetClass) throws ClassNotFoundException {
        Set<String> answerSet = createAnswerSet(template);
        Set<String> targetSet = createTargetSet(targetClass);
        return answerSet.stream().collect(Collectors.toMap(
                answer -> answer,
                answer -> getNearestWord(answer, targetSet)
        ));
    }

    private static Set<String> createTargetSet(Class<?> targetClass) throws ClassNotFoundException {
        Set<String> set = new HashSet<>();
        set.addAll(getFieldNameList(targetClass));
        set.addAll(getMethodNameList(targetClass));
        set.add(targetClass.getName());
        return set;
    }

    private static Set<String> createAnswerSet(String template) {
        Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher m = p.matcher(template);

        Set<String> set = new HashSet<>();
        while (m.find()) {
            set.add(m.group(1));
        }
        return set;
    }

    /**
     * 距離が50以上の時のみ候補から最有力候補を返す
     * 全部50以下なら模範解答をそのまま返す
     * @param answer
     * @param candidateSet
     * @return
     */
    private static String getNearestWord(String answer, Set<String> candidateSet) {
        return candidateSet.stream()
                .filter(candidate -> getLevenshteinDistance(answer, candidate) > 50)
                .max(Comparator.comparingInt(s -> getLevenshteinDistance(answer, s)))
                .orElse(answer);
    }

    private static int getLevenshteinDistance(String s1, String s2){
        LevensteinDistance dis =  new LevensteinDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }

    public static List<String> getFieldNameList(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getMethodNameList(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toList());
    }
    public static String readFile(String fileName) throws IOException {
        Path file = Paths.get("src/main/resources/test/" + fileName);
        return Files.lines(file).collect(Collectors.joining(System.lineSeparator()));
    }
}
