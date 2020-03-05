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
        if (scope.calculateResolvedType().describe().contains(".")) {
            return;
        }
        String className = scope.calculateResolvedType().describe();
        String templateString = "${" + className + "." + methodCallExpr.getName() + "}";

        Runnable runner = () -> methodCallExpr.setName(templateString);
        this.runnableList.add(runner);
    }

    @Override
    public void visit(VariableDeclarationExpr classExpr, Void arg) {
        super.visit(classExpr, arg);
        String className = classExpr.calculateResolvedType().describe();
        if (className.contains(".")) {
            return;
        }
        ClassOrInterfaceType type = new ClassOrInterfaceType() {
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
                return "aaa";
            }

            @Override
            public SimpleName getName() {
                return new SimpleName("${" + className + "}");
            }
        };

        Runnable runner = () -> classExpr.setAllTypes(type);
        this.runnableList.add(runner);
    }
}
