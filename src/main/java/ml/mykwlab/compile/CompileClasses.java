package ml.mykwlab.compile;

import java.util.List;

public class CompileClasses {
    private Class<?> targetClass;
    private List<Class<?>> otherClassList;

    public CompileClasses(Class<?> targetClass, List<Class<?>> otherClassList) {
        this.targetClass = targetClass;
        this.otherClassList = otherClassList;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public List<Class<?>> getOtherClassList() {
        return otherClassList;
    }
}
