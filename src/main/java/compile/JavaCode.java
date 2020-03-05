package compile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class JavaCode extends SimpleJavaFileObject {
    private final String name;
    private final String code;
    public JavaCode(String name, String code) {
        super(getStringURI(name), Kind.SOURCE);
        this.name = name;
        this.code = code;
    }
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
    private static URI getStringURI(final String name) {
        return URI.create("string:///" + name.replace('.','/') +
                Kind.SOURCE.extension);
    }
    public String getName() {
        return this.name;
    }
}
