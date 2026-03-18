package org.bukkit.lazybukkit.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * LazyBukkit Server Bootstrap.
 * <p>
 * This is the entry point when running {@code java -jar lazybukkit.jar}.
 * It extracts the embedded Paper server and LazyBukkit core plugin,
 * then launches Paper within the same JVM process (preserving -Xmx etc.).
 */
public class LazyBukkitBootstrap {

    private static final String VERSION = "1.0.0";
    private static final String PAPER_RESOURCE = "/server/paper.jar";
    private static final String PLUGIN_RESOURCE = "/server/LazyBukkit-Core.jar";

    public static void main(String[] args) throws Exception {
        System.out.println();
        System.out.println("  LazyBukkit Server v" + VERSION);
        System.out.println("  Powered by Paper");
        System.out.println();

        File workDir = new File(".").getCanonicalFile();
        File cacheDir = new File(workDir, ".lazybukkit");
        cacheDir.mkdirs();

        // Extract Paper server (only if missing — don't overwrite on every boot)
        File paperJar = new File(cacheDir, "paper.jar");
        if (!paperJar.exists()) {
            System.out.println("[LazyBukkit] Extracting Paper server...");
            extractResource(PAPER_RESOURCE, paperJar);
            System.out.println("[LazyBukkit] Paper server extracted.");
        }

        // Always update core plugin (so builds are reflected immediately)
        File pluginsDir = new File(workDir, "plugins");
        pluginsDir.mkdirs();
        File pluginJar = new File(pluginsDir, "LazyBukkit-Core.jar");
        System.out.println("[LazyBukkit] Installing core plugin...");
        extractResource(PLUGIN_RESOURCE, pluginJar);

        // Auto-accept EULA on first run
        File eulaFile = new File(workDir, "eula.txt");
        if (!eulaFile.exists()) {
            PrintWriter pw = new PrintWriter(eulaFile);
            pw.println("# LazyBukkit auto-accepted EULA");
            pw.println("# https://aka.ms/MinecraftEULA");
            pw.println("eula=true");
            pw.close();
            System.out.println("[LazyBukkit] Minecraft EULA accepted.");
        }

        // Read Paper's Main-Class from its manifest
        String mainClassName = getMainClass(paperJar);
        System.out.println("[LazyBukkit] Launching Paper (" + mainClassName + ")...");
        System.out.println();

        // Load Paper in a fresh classloader (same JVM = same -Xmx, -Xms, etc.)
        URLClassLoader paperLoader = new URLClassLoader(
            new URL[]{paperJar.toURI().toURL()},
            ClassLoader.getSystemClassLoader().getParent()
        );
        Thread.currentThread().setContextClassLoader(paperLoader);

        Class<?> paperMain = paperLoader.loadClass(mainClassName);
        paperMain.getMethod("main", String[].class).invoke(null, (Object) args);
    }

    private static String getMainClass(File jar) throws Exception {
        JarFile jf = new JarFile(jar);
        try {
            String mc = jf.getManifest().getMainAttributes().getValue("Main-Class");
            if (mc == null) {
                throw new RuntimeException("Paper jar has no Main-Class in manifest");
            }
            return mc;
        } finally {
            jf.close();
        }
    }

    private static void extractResource(String resource, File target) throws Exception {
        InputStream in = LazyBukkitBootstrap.class.getResourceAsStream(resource);
        if (in == null) {
            throw new FileNotFoundException(
                "Embedded resource not found: " + resource
                + ". The lazybukkit.jar may be corrupt — rebuild with ./build.sh");
        }
        try {
            target.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(target);
            try {
                byte[] buf = new byte[65536];
                int n;
                while ((n = in.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
