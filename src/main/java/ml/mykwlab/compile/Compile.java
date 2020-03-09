package ml.mykwlab.compile;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compile {
    public static CompileClasses compile(String target, List<String> others) throws CompileException {
        JavaCode targetCode = createJavaCodeFromCode(target);
        List<JavaCode> otherCodes = new ArrayList<>();
        for (String other : others) {
            JavaCode javaCodeFromCode = createJavaCodeFromCode(other);
            otherCodes.add(javaCodeFromCode);
        }
        return compileJavaCode(targetCode, otherCodes);
    }

    private static JavaCode createJavaCodeFromCode(String code) throws CompileException {
        String className = extraClassNameFromCode(code);
        return new JavaCode(className, code);
    }

    /**
     * クラス名をソースコード上から取得
     * 取得の仕方が甘い
     * コメントとかを挿入されたら厳しい
     * @param code ソースコード
     */
    private static String extraClassNameFromCode(String code) throws CompileException {
        Pattern p = Pattern.compile("public\\s+class\\s+([a-zA-Z]+)");
        Matcher m = p.matcher(code);
        if (m.find()) {
            return m.group(1);
        } else {
            throw new CompileException("no class name");
        }
    }

    private static CompileClasses compileJavaCode(JavaCode target, List<JavaCode> other) throws CompileException {
        List<JavaCode> list = new ArrayList<>();
        list.add(target);
        list.addAll(other);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager manager = new ClassFileManager(compiler);

        Writer writer = new StringWriter();

        boolean isCompileSuccess = manager.getCompiler().getTask(
                writer,
                manager,
                null,
                null,
                null,
                list
        ).call();

        if (!isCompileSuccess) {
            throw new CompileException(writer.toString());
        }

        ClassLoader classLoader = manager.getClassLoader(null);

        try {
            Class<?> targetClass = classLoader.loadClass(target.getName());
            List<Class<?>> otherClassList = new ArrayList<>();
            for (JavaCode javaCode : other) {
                Class<?> clazz = classLoader.loadClass(javaCode.getName());
                otherClassList.add(clazz);
            }
            return new CompileClasses(targetClass, otherClassList);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}