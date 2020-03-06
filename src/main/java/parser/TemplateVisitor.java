package parser;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ユニットテストをテンプレート化するビジター
 * visitしてる時に随時変更していくと解析が失敗してしまうので、
 * runnableListに変更箇所をためておいて解析後に変更する
 *
 * 参考
 * https://qiita.com/opengl-8080/items/50ddee7d635c7baee0ab
 * https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/com/github/javaparser/ast/visitor/VoidVisitorAdapter.html
 */
public class TemplateVisitor extends VoidVisitorAdapter<Void> {
    List<Runnable> runnableList = new ArrayList<>();

    @Override
    public void visit(MethodCallExpr methodCallExpr, Void arg) {
        super.visit(methodCallExpr, arg);
        Expression scope = methodCallExpr.getScope().orElse(null);
        if (scope == null) {
            return;
        }
        String className = scope.calculateResolvedType().describe();
        if (className.contains(".")) {
            return;
        }
        String templateString = "${" + className + ".METHOD." + methodCallExpr.getName() + "}";

        Runnable runner = () -> methodCallExpr.setName(templateString);
        this.runnableList.add(runner);
    }

    @Override
    public void visit(VariableDeclarationExpr variable, Void arg) {
        super.visit(variable, arg);
        String className = variable.calculateResolvedType().describe();
        if (className.contains(".")) {
            return;
        }
        ClassOrInterfaceType type = createClassType(className);

        Runnable runner = () -> variable.setAllTypes(type);
        this.runnableList.add(runner);
    }

    @Override
    public void visit(ObjectCreationExpr constructor, Void arg) {
        super.visit(constructor, arg);
        String className = constructor.calculateResolvedType().describe();
        if (className.contains(".")) {
            return;
        }
        ClassOrInterfaceType type = createClassType(className);

        Runnable runner = () -> constructor.setType(type);
        this.runnableList.add(runner);
    }

    private ClassOrInterfaceType createClassType(String className) {
        return new ClassOrInterfaceType() {
            @Override
            public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
                return v.visit(this, arg);
            }

            @Override
            public <A> void accept(VoidVisitor<A> v, A arg) {
                v.visit(this, arg);
            }

            @Override
            public String asString() {
                return "${" + className + ".CLASS." + className + "}";
            }

            @Override
            public SimpleName getName() {
                return new SimpleName("${" + className + ".CLASS." + className + "}");
            }
        };
    }
}
