package creator;

import org.apache.commons.text.StringSubstitutor;
import org.apache.lucene.search.spell.LevensteinDistance;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Creator {

    public static String templateToJava(String template, Class<?> targetClass) {
        Map<String, String> map = createMap(template, targetClass);
        return new StringSubstitutor(map).replace(template);
    }

    private static Map<String, String> createMap(String template, Class<?> targetClass) {
        Set<String> answerSet = createAnswerSet(template);
        Set<String> targetSet = createTargetSet(targetClass);
        return answerSet.stream().collect(Collectors.toMap(
                answer -> answer,
                answer -> getNearestWord(answer, targetSet)
        ));
    }

    private static Set<String> createTargetSet(Class<?> targetClass) {
        Set<String> set = new HashSet<>();
        set.addAll(getFieldNameList(targetClass));
        set.addAll(getMethodNameList(targetClass));
        set.add(targetClass.getName());
        return set;
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
     * @param candidateSet 候補のセット
     * @return 最有力候補
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

    private static List<String> getFieldNameList(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private static List<String> getMethodNameList(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toList());
    }
}
