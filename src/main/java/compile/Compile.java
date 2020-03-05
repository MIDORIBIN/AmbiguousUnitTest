package compile;

import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;
import entity.Result;
import entity.Results;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Compile {
    public static Results compileAndRun(JavaCode target, List<JavaCode> other) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ClassFileManager manager = new ClassFileManager(compiler);

        // コンパイル
        Result compileResult = compile(target, other, manager);

        // 実行
        Result unitTestResult = new Result(false, "Not Running");
        if (compileResult.getIsSuccess()) {
            unitTestResult = runUnitTest(manager, target.getName());
        }

        try {
            manager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Results(compileResult, unitTestResult);
    }

    public static Result compile(JavaCode target, List<JavaCode> other, ClassFileManager manager) {
        // コンパイル
        List<JavaCode> list = new ArrayList<>();
        list.add(target);
        list.addAll(other);
        return compileOnly(manager, list);
    }

    public static Class<?> compile2(JavaCode target, List<JavaCode> other) throws CompilerException, ClassNotFoundException {
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

        return manager.getClassLoader(null).loadClass(target.getName());

//        return new Result(isCompileSuccess, writer.toString());
    }

    private static Result compileOnly(ClassFileManager manager, List<JavaCode> javaFileList) {
        Writer writer = new StringWriter();

        boolean isCompileSuccess = manager.getCompiler().getTask(
                writer,
                manager,
                null,
                null,
                null,
                javaFileList
        ).call();

        return new Result(isCompileSuccess, writer.toString());
    }

    /**
     * ユニットテスト
     * 標準出力の問題からこのメソッドは同時に呼ばれないようになってる
     * @param manager　マネージャ
     * @param className 実効対象（testファイル）のクラス名
     * @return ユニットテストの評価結果
     */
    public synchronized static Result runUnitTest(JavaFileManager manager, String className) {
        ClassLoader cl = manager.getClassLoader(null);

        Class<?> clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        PrintStream defaultPrintStream = System.out;

        JUnitCore jUnitCore = new JUnitCore();
        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();
        PrintStream resultStream = new PrintStream(resultBytes);
        jUnitCore.addListener(new TextListener(resultStream));

        org.junit.runner.Result result = jUnitCore.run(clazz);
        System.setOut(defaultPrintStream);

        if (result.getFailureCount() > 0) {
            String output = "";
            try {
                output = resultBytes.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            System.out.println("=============");
            System.out.println(output);
            System.out.println("=============");
        }

        // Tests run: 3,  Failures: 3
        String message = "Tests " +
                "run: " + result.getRunCount() + ", " +
                "Failures: " + result.getFailureCount();

        // 無限ループ対策
        stopTestThread();

        return new Result(result.wasSuccessful(), message);
    }

    /**
     * 全てのスレッドを探索して、Time-limited testの名前のスレッドを強制終了
     */
    @SuppressWarnings("deprecation")
    private static void stopTestThread() {
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        for (Thread thread: threadSet) {
            if (thread.getName().equals("Time-limited test")) {
                thread.stop();
            }
        }
    }
}