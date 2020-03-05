package parser;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class Main2 {
    public static void main(String[] args) {
        String targetDir = "src/main/resources/test/";
        Path targetSourcePath = Paths.get(targetDir + "RucksackTest.java");
        Path dependenceSourcePath = Paths.get(targetDir);
        Path jarPath = Paths.get("C:/Users/kondo/.gradle/caches/modules-2/files-2.1/junit/junit/4.13/e49ccba652b735c93bd6e6f59760d8254cf597dd/junit-4.13.jar");

        try {
            CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();

            combinedTypeSolver.add(new ReflectionTypeSolver());
            // junit
            JarTypeSolver jarTypeSolver = new JarTypeSolver(jarPath);
            combinedTypeSolver.add(jarTypeSolver);
            // 依存ファイル
            JavaParserTypeSolver javaParserTypeSolver = new JavaParserTypeSolver(dependenceSourcePath);
            combinedTypeSolver.add(javaParserTypeSolver);

            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
            StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

            CompilationUnit unit = StaticJavaParser.parse(targetSourcePath);
            System.out.println("***********************************************");

            TemplateVisitor templateVisitor = new TemplateVisitor();
            unit.accept(templateVisitor, null);
            templateVisitor.runnableList.forEach(Runnable::run);

//            for (JavaToken javaToken : unit.getTokenRange().get()) {
//                System.out.println(javaToken.getText() + javaToken.getCategory());
//            }

            System.out.println("***********************************************");

            System.out.println(unit);
            Files.write(new File(targetDir + "Modified.java_template").toPath(), Collections.singleton(unit.toString()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
