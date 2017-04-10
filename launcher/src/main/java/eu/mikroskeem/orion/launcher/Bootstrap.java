package eu.mikroskeem.orion.launcher;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import eu.mikroskeem.orion.launcher.util.LibraryManager;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.ClassWrapper;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import sun.misc.URLClassPath;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static eu.mikroskeem.orion.launcher.util.LibraryManager.Library;
import static eu.mikroskeem.orion.launcher.util.LibraryManager.RuntimeLibrary;

/**
 * Orion Launcher wrapper for server, based on Minecraft LegacyLauncher
 *
 * @author Mark Vainomaa
 * @version 0.0.1
 */
@Slf4j
@SuppressWarnings("unchecked")
public class Bootstrap {
    public static LibraryManager libraryManager = new LibraryManager();

    @SneakyThrows
    public static void main(String... args) {
        /* Set up SLF4J configuration */
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(Bootstrap.class.getResourceAsStream("/orion_logback.xml"));
        } catch (JoranException e){
            e.printStackTrace();
        }

        log.info("Orion Launcher by mikroskeem");
        List<String> finalArgs = new ArrayList<>(Arrays.asList(
                "--tweakClass",
                "eu.mikroskeem.orion.launcher.OrionTweakClass"
        ));
        finalArgs.addAll(Arrays.asList(args));

        /* Set up libraries */
        log.info("Checking runtime libraries...");
        libraryManager.addAllLibraries(Arrays.asList(
                /* Server libraries */
                new Library("com.typesafe", "config", "1.3.0", null),
                new Library("ninja.leaping.configurate", "configurate-core", "3.2", null),
                new Library("ninja.leaping.configurate", "configurate-hocon", "3.2", null),
                new Library("org.codehaus.groovy", "groovy-all", "2.4.10", null),
                new Library("com.fasterxml.jackson.core", "jackson-core", "2.8.7", null),
                new Library("com.getsentry.raven", "raven", "8.0.1", null),
                new Library("com.github.ben-manes.caffeine", "caffeine", "2.4.0", null),

                new RuntimeLibrary("org.apache.commons", "commons-lang3", "3.5", null),
                new RuntimeLibrary("commons-io", "commons-io", "2.4", null),
                new RuntimeLibrary("io.github.lukehutch", "fast-classpath-scanner", "2.0.18", null),
                new RuntimeLibrary("com.google.guava", "guava", "17.0", null),
                new RuntimeLibrary("com.google.code.gson", "gson", "2.2.4", null),
                new RuntimeLibrary("org.ow2.asm", "asm-all", "5.2", null),
                new RuntimeLibrary("org.apache.logging.log4j", "log4j-api", "2.0-beta9", null),
                new RuntimeLibrary("org.apache.logging.log4j", "log4j-core", "2.0-beta9", null),
                new RuntimeLibrary("net.sf.jopt-simple", "jopt-simple", "4.9", null),

                new RuntimeLibrary("net.minecraft", "launchwrapper", "1.13-mikroskeem", null),
                new RuntimeLibrary("org.spongepowered", "mixin", "0.6.8-SNAPSHOT", null,
                        new LibraryManager.MavenExtraData("20170320.130808", 7))

        ));
        libraryManager.run();

        /* Build libraries URL list */
        List<URL> libraries = new ArrayList<>();
        libraries.add(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation());
        libraryManager.getRuntimeLibraries()
                .stream()
                .map(Library::getLocalPath)
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (MalformedURLException e){
                        log.error("Malformed URL: {}", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(libraries::add);

        /*
         * Load libraries
         *
         * Some code is from
         * https://github.com/DemonWav/BukkitStart/blob/master/src/main/java/StartServer.java#L61
         */
        log.info("Loading libraries");
        URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        ClassWrapper<URLClassLoader> uclWrapper = Reflect.wrapInstance(loader);
        URLClassPath ucp = uclWrapper.getField("ucp", URLClassPath.class).get().read();
        ClassWrapper<URLClassPath> ucpWrapper = Reflect.wrapInstance(ucp);

        HashMap<String, Closeable> lmap = (HashMap<String, Closeable>) ucpWrapper.getField("lmap", HashMap.class).get().read();
        ArrayList<Closeable> loaders = (ArrayList<Closeable>) ucpWrapper.getField("loaders", ArrayList.class).get().read();

        /* Add urls */
        libraries.forEach(url -> {
            try {
                ucpWrapper.invokeMethod("addURL", void.class, TypeWrapper.of(url));
                Closeable ldr = (Closeable)ucpWrapper.invokeMethod("getLoader", Object.class, TypeWrapper.of(url));
                loaders.add(ldr);
                lmap.put("file://" + url.getFile(), ldr);
            }
            catch (Exception e){
                SneakyThrow.throwException(e);
            }
        });

        /* Re-enable lookup cache (the addURL will disable it) */
        ucpWrapper.getField("lookupCacheEnabled", boolean.class).get().write(true);

        /* Force cache repopulation */
        ucpWrapper.getField("lookupCacheURLs", Object.class).get().write(null);
        ucpWrapper.getField("lookupCacheLoader", ClassLoader.class).get().write(null);

        /* Start LegacyLauncer */
        Optional<ClassWrapper<?>> launchClassOpt = Reflect.getClass("net.minecraft.launchwrapper.Launch");
        if(launchClassOpt.isPresent()) {
            ClassWrapper<?> launchClass = launchClassOpt.get();
            try {
                log.info("Starting LegacyLauncher with arguments: {}", finalArgs);
                launchClass.invokeMethod("main", void.class,
                        TypeWrapper.of(String[].class, finalArgs.toArray(new String[0])));
            } catch (Exception e) {
                log.error("Failed to start LegacyLauncher! {}", e);
            }
        } else {
            log.error("Failed to find class 'net.minecraft.launchwrapper.Launch'!");
        }
    }
}
