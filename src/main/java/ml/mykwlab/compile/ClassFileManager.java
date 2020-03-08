package ml.mykwlab.compile;

import javax.tools.*;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

public final class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, JavaClassObject> classMap = new HashMap<>();
    private final JavaCompiler compiler;
    private ClassLoader classLoader = new SecureClassLoader() {
        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            if (!classMap.containsKey(name)) {
                return super.findClass(name);
            }

            JavaClassObject javaClass = classMap.get(name);
            Class<?> clazz = javaClass.getDefinedClass();

            if (clazz == null) {
            byte[] b = javaClass.getBytes();
                clazz = super.defineClass(name, b, 0, b.length);
                javaClass.setDefinedClass(clazz);
            }
            return clazz;
        }
    };

    public ClassFileManager(final JavaCompiler javaCompiler) {
        super(javaCompiler.getStandardFileManager(null, null, null));
        this.compiler = javaCompiler;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            final Location location,
            final String className,
            final JavaFileObject.Kind kind,
            final FileObject sibling
    ) {
        classMap.put(className, new JavaClassObject(className, kind));
        return classMap.get(className);
    }

    @Override
    public ClassLoader getClassLoader(final Location location) {
        return classLoader;
    }

    public JavaCompiler getCompiler() {
        return this.compiler;
    }
}
