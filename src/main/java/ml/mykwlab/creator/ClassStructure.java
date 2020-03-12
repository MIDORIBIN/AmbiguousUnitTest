package ml.mykwlab.creator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

class ClassStructure {
    private String className;
    private Set<String> fieldSet;
    private Set<String> methodSet;

    public ClassStructure(Class<?> clazz) {
        this.className = clazz.getName();
        this.fieldSet = getFieldNameSet(clazz);
        this.methodSet = getMethodNameSet(clazz);
    }

    public String getClassName() {
        return this.className;
    }

    public Set<String> getFieldSet() {
        return this.fieldSet;
    }

    public Set<String> getMethodSet() {
        return this.methodSet;
    }

    private static Set<String> getFieldNameSet(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    private static Set<String> getMethodNameSet(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());
    }
}
