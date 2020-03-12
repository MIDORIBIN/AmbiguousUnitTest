package ml.mykwlab.creator;


import ml.mykwlab.compile.CompileClasses;
import ml.mykwlab.compile.CompileException;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ml.mykwlab.compile.Compile.compile;

class TestCreateUtil {
    static List<String> splitTemplate(String template) {
        return Arrays.stream(template.split("(?=( {4}@Test))"))
                .flatMap(str -> cutNextBlock(str).stream())
                .collect(Collectors.toList());
    }

    private static List<String> cutNextBlock(String lines) {
        Pattern pattern = Pattern.compile(System.lineSeparator() + " {4}}");
        Matcher matcher = pattern.matcher(lines);
        if (!matcher.find()) {
            return Collections.singletonList(lines);
        }

        int index = matcher.end();
        List<String> list = new ArrayList<>();

        list.add(lines.substring(0, index));
        list.add(lines.substring(index));

        return list;
    }

    static Set<ClassStructure> createClassStructureSet(String target, List<String> others) throws CompileException {
        CompileClasses compileClasses = compile(target, others);

        Set<ClassStructure> classStructureSet = new HashSet<>();
        classStructureSet.add(new ClassStructure(compileClasses.getTargetClass()));
        compileClasses.getOtherClassList()
                .forEach(other -> classStructureSet.add(new ClassStructure(other)));
        return classStructureSet;
    }

    static Map<String, String> createAmbiguousMap(String template, Set<ClassStructure> classStructureSet) {
        Set<String> answerSet = createAnswerSet(template);

        return answerSet.stream().collect(Collectors.toMap(
                answer -> answer,
                answer -> getNearestWord(answer, classStructureSet)
        ));
    }

    private static Set<String> createAnswerSet(String template) {
        Pattern p = Pattern.compile("\\$\\{([^}]+)}");
        Matcher m = p.matcher(template);

        Set<String> set = new HashSet<>();
        while (m.find()) {
            set.add(m.group(1));
        }
        return set;
    }

    /**
     * 距離が70以上の時のみ候補から最有力候補を返す
     * 全部70以下なら空文字を返す
     * @param answer 模範解答
     * @param classStructureSet 候補のセット
     * @return 最有力候補
     */
    private static String getNearestWord(String answer, Set<ClassStructure> classStructureSet) {
        String[] line = answer.split("\\.");
        String answerClassName = line[0];
        String structureType = line[1];
        String structureName = line[2];

        return selectClassStructure(answerClassName, classStructureSet)
                .map(classStructure -> {
                    if ("CLASS".equals(structureType)) {
                        return classStructure.getClassName();
                    }
                    if ("METHOD".equals(structureType)) {
                        return selectWord(structureName, classStructure.getMethodSet()).orElse("");
                    }
                    if ("FIELD".equals(structureType)) {
                        return selectWord(structureName, classStructure.getFieldSet()).orElse("");
                    }
                    return null;
                }).orElse("");
    }

    private static Optional<ClassStructure> selectClassStructure(String answerClassName, Set<ClassStructure> classStructureSet) {
        return classStructureSet.stream()
                .max(Comparator.comparingInt(structure -> getLevenshteinDistance(answerClassName, structure.getClassName())));
    }

    private static Optional<String> selectWord(String answerName, Set<String> wordSet) {
        return wordSet.stream()
                .filter(word -> getLevenshteinDistance(answerName, word) > 70)
                .max(Comparator.comparingInt(word -> getLevenshteinDistance(answerName, word)));
    }

    private static int getLevenshteinDistance(String s1, String s2){
        LevensteinDistance dis =  new LevensteinDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }
}
