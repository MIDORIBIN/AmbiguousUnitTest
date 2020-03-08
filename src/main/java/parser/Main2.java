package parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class Main2 {
    public static void main(String[] args) throws IOException {
        String targetTestFileName = "RucksackTest.java";

        String targetDir = "src/main/resources/test/";
        String dependenceFileName = targetTestFileName.replace("Test", "");
        Path dependencePath = Paths.get(targetDir + dependenceFileName);
        Path targetTestPath = Paths.get(targetDir + targetTestFileName);
        initConfig(Paths.get(targetDir));

        System.out.println("***********************************************");

        Set<String> fieldNameList = getFieldNameSet(dependencePath);

        CompilationUnit unit = StaticJavaParser.parse(targetTestPath);
        TemplateVisitor templateVisitor = new TemplateVisitor(dependenceFileName.substring(0, dependenceFileName.length() - 5), fieldNameList);
        unit.accept(templateVisitor, null);
        templateVisitor.getRunnableList().forEach(Runnable::run);

        System.out.println("***********************************************");

//        System.out.println(unit);
        Files.write(new File(targetDir + "Modified.java_template").toPath(), Collections.singleton(unit.toString()), StandardCharsets.UTF_8);
    }

    private static void initConfig(Path dependenceSourcePath) throws IOException {
        Path jarPath = Paths.get("src/main/java/parser/junit-4.13.jar");

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
    }

    private static Set<String> getFieldNameSet(Path path) throws IOException {
        CompilationUnit unit = StaticJavaParser.parse(path);
        return unit.findAll(FieldDeclaration.class).stream()
                .map(FieldDeclaration::getVariables)
                .flatMap(Collection::stream)
                .map(VariableDeclarator::getName)
                .map(Node::toString)
                .collect(Collectors.toSet());
    }
}
