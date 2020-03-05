package parser;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.github.javaparser.JavaToken.Category.IDENTIFIER;

public class Main {
    public static void main(String[] args) {
//        Path source = Paths.get("src/main/java/parser.Gum.java");
        Path source = Paths.get("src/main/java/test.txt");
        try {
            CompilationUnit unit = StaticJavaParser.parse(source);
            System.out.println("***********************************************");
            unit.getTokenRange()
                    .ifPresent(Main::test);

            System.out.println("***********************************************");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void test(TokenRange tokens) {
        List<String> list = StreamSupport.stream(tokens.spliterator(), false)
                .map(Main::fixIdentifier)
                .map(JavaToken::getText)
                .collect(Collectors.toList());

        list.forEach(System.out::print);
    }

    public static JavaToken fixIdentifier(JavaToken javaToken) {
        Map<String, String> map = new HashMap<>();
        map.put("PlasticTray", "Plastictray");

        if (javaToken.getCategory().equals(IDENTIFIER)) {
            return javaToken;
        }
        // if String
        if (javaToken.getKind() == 89) {
            String text = javaToken.getText().replace("\"", "");
            if (map.containsKey(text)) {
                javaToken.setText("\"" + map.get(text) + "\"");
            }
            return javaToken;
        }
        return javaToken;

    }
}
