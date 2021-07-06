package libraryManager;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

public class SelfFirstClassLoader {

    private static Instrumentation inst = null;

    /** The JRE will call method before launching your main() */
    public static void agentmain(final String a, final Instrumentation inst) {
        SelfFirstClassLoader.inst = inst;
    }

    public SelfFirstClassLoader(ClassLoader load) {
        try {
            if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
                Method f = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                f.setAccessible(true);
                f.invoke(ClassLoader.getSystemClassLoader(), new File("/mnt/l/a.jar").toURI().toURL());
            } else {
                inst.appendToSystemClassLoaderSearch(new JarFile(new File("/mnt/l/a.jar")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
