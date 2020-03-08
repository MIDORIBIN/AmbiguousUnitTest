package ml.mykwlab.parser;

import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    private List<Runnable> runnableList = new ArrayList<>();
    private String dependenceClassName;
    private Set<String> fieldNameSet;

    public TemplateVisitor(String dependenceClassName, Set<String> fieldNameSet) {
        this.dependenceClassName = dependenceClassName;
        this.fieldNameSet = fieldNameSet;
    }

    public List<Runnable> getRunnableList() {
        return this.runnableList;
    }

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

    @Override
    public void visit(ClassOrInterfaceType constructor, Void arg) {
        super.visit(constructor, arg);
        String typeName = constructor.asString();
        if (!typeName.contains("<") || !typeName.contains(">")) {
            return;
        }
        String typeParameterName = constructor.getTypeArguments().get().get(0).asString();
        Runnable runner = () -> constructor.setTypeArguments(createClassType(typeParameterName));
        this.runnableList.add(runner);
    }

    @Override
    public void visit(ClassExpr classExpr, Void arg) {
        String packageClassName = classExpr.calculateResolvedType().describe();
        String className = packageClassName.substring(16, packageClassName.length() - 1);
        classExpr.setType(createClassType(className));
    }

    @Override
    public void visit(StringLiteralExpr stringLiteral, Void arg) {
        String str = stringLiteral.toString().replace("\"", "");
        if (this.fieldNameSet.contains(str)) {
            String templateLiteral = "${" + this.dependenceClassName + ".FIELD." + str + "}";
            stringLiteral.setString(templateLiteral);
        }
    }

    private static ClassOrInterfaceType createClassType(String className) {
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
