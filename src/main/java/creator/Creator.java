package creator;

import compile.CompileClasses;
import org.apache.commons.text.StringSubstitutor;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Creator {
    public static String templateToJava(String template, CompileClasses compileClasses) {
        Set<ClassStructure> classStructureSet = new HashSet<>();
        classStructureSet.add(new ClassStructure(compileClasses.getTargetClass()));
        compileClasses.getOtherClassList()
                .forEach(other -> classStructureSet.add(new ClassStructure(other)));

        Map<String, String> map = createMap(template, classStructureSet);
        return new StringSubstitutor(map).replace(template);
    }

    private static Map<String, String> createMap(String template, Set<ClassStructure> classStructureSet) {
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
     * 距離が50以上の時のみ候補から最有力候補を返す
     * 全部50以下なら模範解答をそのまま返す
     * @param answer 模範解答
     * @param classStructureSet 候補のセット
     * @return 最有力候補
     */
    private static String getNearestWord(String answer, Set<ClassStructure> classStructureSet) {
//        return candidateSet.stream()
//                .filter(candidate -> getLevenshteinDistance(answer, candidate) > 50)
//                .max(Comparator.comparingInt(s -> getLevenshteinDistance(answer, s)))
//                .orElse(answer);
        String[] line = answer.split("\\.");
        String answerClassName = line[0];
        String structureType = line[1];
        String structureNmae = line[2];

        ClassStructure classStructure = selectClassStructure(answerClassName, classStructureSet);
        if ("CLASS".equals(structureType)) {
            return classStructure.getClassName();
        }
        if ("METHOD".equals(structureType)) {
            return selectWord(structureNmae, classStructure.getMethodSet());
        }
        if ("FIELD".equals(structureType)) {
            return selectWord(structureNmae, classStructure.getFieldSet());
        }
        return null;
    }

    private static ClassStructure selectClassStructure(String answerClassName, Set<ClassStructure> classStructureSet) {
        return classStructureSet.stream()
                .max(Comparator.comparingInt(structure -> getLevenshteinDistance(answerClassName, structure.getClassName())))
                .get();
    }

    private static String selectWord(String answerName, Set<String> wordSet) {
        return wordSet.stream()
                .max(Comparator.comparingInt(word -> getLevenshteinDistance(answerName, word)))
                .get();
    }

    private static int getLevenshteinDistance(String s1, String s2){
        LevensteinDistance dis =  new LevensteinDistance();
        return (int) (dis.getDistance(s1, s2) * 100);
    }
}
