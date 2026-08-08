package org.patchbukkit.loader;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class PatchBukkitPluginClassLoader
    extends URLClassLoader
    implements ConfiguredPluginClassLoader
{

    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private JavaPlugin plugin;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public static final java.util.Set<PatchBukkitPluginClassLoader> ALL_LOADERS =
        new java.util.concurrent.CopyOnWriteArraySet<>();

    public PatchBukkitPluginClassLoader(ClassLoader parent, File file)
        throws MalformedURLException, InvalidDescriptionException {
        this(parent, file, new URL[0]);
    }

    public PatchBukkitPluginClassLoader(
        ClassLoader parent,
        File file,
        URL[] extraUrls
    ) throws MalformedURLException, InvalidDescriptionException {
        super(buildUrls(file, extraUrls), parent);
        this.file = file;
        this.description = loadDescription(file);
        this.dataFolder = new File(file.getParentFile(), description.getName());
        ALL_LOADERS.add(this);

        File libsDir = new File(file.getParentFile(), "patchbukkit-libs");
        if (!libsDir.exists()) {
            libsDir.mkdirs();
        }

        // Extract and load nested JARs inside plugin JAR
        for (File nestedJar : extractNestedJars(file, libsDir)) {
            try {
                addURL(nestedJar.toURI().toURL());
            } catch (MalformedURLException ignored) {}
        }

        List<String> libsToResolve = new ArrayList<>();
        if (this.description.getLibraries() != null) {
            libsToResolve.addAll(this.description.getLibraries());
        }
        for (String lib : extractLibraries(file)) {
            if (!libsToResolve.contains(lib)) {
                libsToResolve.add(lib);
            }
        }

        if (!libsToResolve.isEmpty()) {
            List<File> resolved = LibraryResolver.resolveLibraries(
                String.join("\n", libsToResolve),
                libsDir
            );
            for (File lib : resolved) {
                if (lib != null && lib.exists()) {
                    try {
                        addURL(lib.toURI().toURL());
                    } catch (MalformedURLException ignored) {}
                }
            }
        }
    }

    private static List<File> extractNestedJars(File file, File libsDir) {
        List<File> extractedFiles = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".jar")) {
                    if (name.startsWith("META-INF/jars/") ||
                        name.startsWith("paper-libraries/") ||
                        name.startsWith("BOOT-INF/lib/") ||
                        name.startsWith("lib/") ||
                        name.startsWith("libraries/")) {
                        
                        File targetFile = new File(libsDir, new File(name).getName());
                        if (!targetFile.exists()) {
                            try (InputStream is = jar.getInputStream(entry);
                                 java.io.FileOutputStream fos = new java.io.FileOutputStream(targetFile)) {
                                is.transferTo(fos);
                            }
                        }
                        if (targetFile.exists()) {
                            extractedFiles.add(targetFile);
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return extractedFiles;
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractLibraries(File file) {
        List<String> libraries = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            // 1. Try paper-libraries.json
            JarEntry entryJson = jar.getJarEntry("paper-libraries.json");
            if (entryJson != null) {
                try (InputStream is = jar.getInputStream(entryJson)) {
                    org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
                    Object obj = yaml.load(is);
                    if (obj instanceof Map<?, ?> data) {
                        Object repos = data.get("repositories");
                        if (repos instanceof Map<?, ?> repoMap) {
                            for (Object v : repoMap.values()) {
                                if (v != null && !v.toString().isBlank()) {
                                    String r = v.toString().trim();
                                    if (!libraries.contains("repo:" + r)) {
                                        libraries.add("repo:" + r);
                                    }
                                }
                            }
                        } else if (repos instanceof List<?> repoList) {
                            for (Object o : repoList) {
                                if (o != null) {
                                    String r = o.toString().trim();
                                    if (o instanceof Map<?, ?> m && m.containsKey("url")) {
                                        r = m.get("url").toString().trim();
                                    }
                                    if (!libraries.contains("repo:" + r)) {
                                        libraries.add("repo:" + r);
                                    }
                                }
                            }
                        }
                        Object deps = data.get("dependencies");
                        if (deps instanceof List<?> list) {
                            for (Object o : list) {
                                if (o != null && !libraries.contains(o.toString())) {
                                    libraries.add(o.toString());
                                }
                            }
                        } else if (deps instanceof Map<?, ?> map) {
                            for (Map.Entry<?, ?> e : map.entrySet()) {
                                if (e.getKey() != null && e.getValue() != null) {
                                    String coord = e.getKey().toString() + ":" + e.getValue().toString();
                                    if (!libraries.contains(coord)) {
                                        libraries.add(coord);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // 2. Try paper-libraries.list
            JarEntry entryList = jar.getJarEntry("paper-libraries.list");
            if (entryList != null) {
                try (InputStream is = jar.getInputStream(entryList);
                     java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#")) {
                            if (!libraries.contains(line)) {
                                libraries.add(line);
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // 3. Try paper-plugin.yml or plugin.yml
            JarEntry entryYaml = jar.getJarEntry("paper-plugin.yml");
            if (entryYaml == null) {
                entryYaml = jar.getJarEntry("plugin.yml");
            }
            if (entryYaml != null) {
                try (InputStream is = jar.getInputStream(entryYaml)) {
                    org.yaml.snakeyaml.Yaml yaml = new org.yaml.snakeyaml.Yaml();
                    Object obj = yaml.load(is);
                    if (obj instanceof Map<?, ?> data) {
                        Object libs = data.get("libraries");
                        if (libs instanceof List<?> list) {
                            for (Object o : list) {
                                if (o != null && !libraries.contains(o.toString())) {
                                    libraries.add(o.toString());
                                }
                            }
                        }
                        Object repos = data.get("repositories");
                        if (repos instanceof Map<?, ?> repoMap) {
                            for (Object v : repoMap.values()) {
                                if (v != null && !v.toString().isBlank()) {
                                    String r = v.toString().trim();
                                    if (!libraries.contains("repo:" + r)) {
                                        libraries.add("repo:" + r);
                                    }
                                }
                            }
                        } else if (repos instanceof List<?> repoList) {
                            for (Object o : repoList) {
                                if (o != null) {
                                    String r = o.toString().trim();
                                    if (o instanceof Map<?, ?> m && m.containsKey("url")) {
                                        r = m.get("url").toString().trim();
                                    }
                                    if (!libraries.contains("repo:" + r)) {
                                        libraries.add("repo:" + r);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return libraries;
    }

    private static URL[] buildUrls(File file, URL[] extraUrls)
        throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        urls.add(file.toURI().toURL());
        if (extraUrls != null) {
            for (URL url : extraUrls) {
                if (url != null) {
                    urls.add(url);
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    private static PluginDescriptionFile loadDescription(File file)
        throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("paper-plugin.yml");
            if (entry == null) {
                entry = jar.getJarEntry("plugin.yml");
            }
            if (entry == null) {
                throw new InvalidDescriptionException(
                    "Jar does not contain plugin.yml"
                );
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(stream);
            }
        } catch (IOException e) {
            throw new InvalidDescriptionException(e);
        }
    }

    /**
     * Child-first class loading: try to load from plugin JAR before delegating to parent.
     * This is critical for plugin classes to be loaded by this classloader.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // First, check if already loaded
            Class<?> c = findLoadedClass(name);

            if (c == null) {
                // For plugin-specific classes, try to load from JAR first (child-first)
                // For JDK and server classes, delegate to parent
                if (
                    !name.startsWith("java.") &&
                    !name.startsWith("jdk.") &&
                    !name.startsWith("sun.") &&
                    !name.startsWith("javax.")
                ) {
                    try {
                        // Try to find in plugin JAR / libraries first
                        c = findClass(name);
                    } catch (ClassNotFoundException e) {
                        // Not in local JAR, check other plugin classloaders in global pool
                        for (PatchBukkitPluginClassLoader other : ALL_LOADERS) {
                            if (other != this) {
                                try {
                                    c = other.findLoadedClass(name);
                                    if (c == null) {
                                        c = other.findClass(name);
                                    }
                                    if (c != null) {
                                        break;
                                    }
                                } catch (ClassNotFoundException ignored) {}
                            }
                        }
                    }
                }

                // If not found in JAR (or is a system class), delegate to parent
                if (c == null) {
                    c = getParent().loadClass(name);
                }
            }

            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }

    @Override
    public PluginMeta getConfiguration() {
        return description;
    }

    @Override
    public Class<?> loadClass(
        String name,
        boolean resolve,
        boolean checkGlobal,
        boolean checkLibraries
    ) throws ClassNotFoundException {
        return loadClass(name, resolve);
    }

    @Override
    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        plugin.init(
            org.bukkit.Bukkit.getServer(),
            description,
            dataFolder,
            file,
            this,
            description,
            com.destroystokyo.paper.utils.PaperPluginLogger.getLogger(
                description
            )
        );
    }

    @Override
    @Nullable
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    @Nullable
    public PluginClassLoaderGroup getGroup() {
        return null;
    }

    public PluginDescriptionFile getDescription() {
        return description;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void close() throws IOException {
        ALL_LOADERS.remove(this);
        super.close();
    }
}
