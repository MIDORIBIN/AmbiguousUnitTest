package compile;

import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Compile {

    public static Class<?> compile(String target, List<String> others) throws CompilerException {
        JavaCode targetCode = createJavaCodeFromCode(target);
        List<JavaCode> otherCodes = others.stream()
                .map(Compile::createJavaCodeFromCode)
                .collect(Collectors.toList());
        return compileJavaCode(targetCode, otherCodes);
    }

    private static JavaCode createJavaCodeFromCode(String code) {
        String className = extraClassNameFromCode(code);
        return new JavaCode(className, code);
    }

    /**
     * クラス名をソースコード上から取得
     * 取得の仕方が甘い
     * コメントとかを挿入されたら厳しい
     * @param code
     */
    private static String extraClassNameFromCode(String code) {
        Pattern p = Pattern.compile("class\\s+([a-zA-Z]+)");
        Matcher m = p.matcher(code);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private static Class<?> compileJavaCode(JavaCode target, List<JavaCode> other) throws CompilerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager manager = new ClassFileManager(compiler);

        List<JavaCode> list = new ArrayList<>();
        list.add(target);
        list.addAll(other);

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
            throw new CompilerException(writer.toString());
        }

        ClassLoader classLoader = manager.getClassLoader(null);

        try {
            return classLoader.loadClass(target.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}