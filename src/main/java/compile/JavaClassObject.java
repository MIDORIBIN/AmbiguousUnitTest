package compile;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class JavaClassObject extends SimpleJavaFileObject {

    private Class<?> clazz;
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    protected JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }
    @Override
    public OutputStream openOutputStream() {
        return bos;
    }
    public byte[] getBytes() {
        return bos.toByteArray();
    }
    public void setDefinedClass(Class<?> clazz) {
        this.clazz = clazz;
    }
    public Class<?> getDefinedClass() {
        return clazz;
    }
}
