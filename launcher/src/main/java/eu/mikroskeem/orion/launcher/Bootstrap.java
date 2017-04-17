package eu.mikroskeem.orion.launcher;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.picomaven.DownloaderCallbacks;
import eu.mikroskeem.picomaven.PicoMaven;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.ClassWrapper;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import sun.misc.URLClassPath;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orion Launcher wrapper for server, based on Minecraft LegacyLauncher
 *
 * @author Mark Vainomaa
 * @version 0.0.1
 */
@Slf4j
@SuppressWarnings("unchecked")
public class Bootstrap {
    static List<URL> serverDependenciesList = new ArrayList<>();

    @SneakyThrows
    public static <UCPLoader extends Closeable> void main(String... args) {
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
        List<Dependency> serverDependencies = Arrays.asList(
                /* Core configuration */
                new Dependency("com.typesafe", "config", "1.3.0"),
                new Dependency("ninja.leaping.configurate", "configurate-core", "3.2"),
                new Dependency("ninja.leaping.configurate", "configurate-hocon", "3.2"),

                /* Debugging and reporting */
                new Dependency("org.codehaus.groovy", "groovy-all", "2.4.10"),
                new Dependency("com.fasterxml.jackson.core", "jackson-core", "2.8.7"),
                new Dependency("com.getsentry.raven", "raven", "8.0.1"),

                /* Caching */
                new Dependency("com.github.ben-manes.caffeine", "caffeine", "2.4.0")
        );
        List<Dependency> runtimeDependencies = Arrays.asList(
                /* Class tools */
                new Dependency("io.github.lukehutch", "fast-classpath-scanner", "2.0.19"),
                new Dependency("org.ow2.asm", "asm-all", "5.2"),

                /* Common utilities */
                new Dependency("org.apache.commons", "commons-lang3", "3.5"),
                new Dependency("commons-io", "commons-io", "2.4"),
                new Dependency("com.google.guava", "guava", "17.0"),
                new Dependency("com.google.code.gson", "gson", "2.2.4"),

                /* LegacyLauncher dependencies */
                new Dependency("org.apache.logging.log4j", "log4j-api", "2.0-beta9"),
                new Dependency("org.apache.logging.log4j", "log4j-core", "2.0-beta9"),
                new Dependency("net.sf.jopt-simple", "jopt-simple", "4.9"),

                new Dependency("net.minecraft", "launchwrapper", "1.13-mikroskeem"),
                new Dependency("org.spongepowered", "mixin", "0.6.8-SNAPSHOT")
        );

        List<URI> repositories = Stream.of(
                "https://repo.maven.apache.org/maven2",                     /* Central */
                "https://repo.wut.ee/repository/mikroskeem-repo",           /* Own repository */
                "http://ci.emc.gs/nexus/content/groups/aikar",              /* aikar's repository */
                "https://repo.spongepowered.org/maven"                      /* SpongePowered repository */
        ).map(URI::create).collect(Collectors.toList());

        /* Build PicoMaven and download dependencies */
        ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("Dependency downloader thread " + THREAD_COUNTER.getAndIncrement());
                return thread;
            }
        });

        PicoMaven.Builder picoMavenBase = new PicoMaven.Builder()
                .withRepositories(repositories)
                .withExecutorService(executorService)
                .shouldCloseExecutorService(false)
                .withDownloadPath(Paths.get("./libraries").toAbsolutePath())
                .withDownloaderCallbacks(new DownloaderCallbacks() {
                    @Override
                    public void onSuccess(Dependency dependency, Path dependencyPath) {
                        log.info("{} download succeeded!", dependency);
                    }

                    @Override
                    public void onFailure(Dependency dependency, IOException exception) {
                        log.error("{} download failed! {}", dependency, exception.getMessage());
                    }
                });

        List<URL> libraries = new ArrayList<>();
        libraries.add(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation());
        try(PicoMaven runtimeDepsDownloader = picoMavenBase.withDependencies(runtimeDependencies).build()) {
            List<Path> downloaded = runtimeDepsDownloader.downloadAll();
            /* Build libraries URL list */
            libraries.addAll(downloaded.stream().map(Bootstrap::convertPath).collect(Collectors.toList()));
        }

        try(PicoMaven serverDepsDownloader = picoMavenBase.withDependencies(serverDependencies).build()) {
            List<Path> downloaded = serverDepsDownloader.downloadAll();
            serverDependenciesList.addAll(downloaded.stream().map(Bootstrap::convertPath).collect(Collectors.toList()));
        }

        /* Shut down executor service */
        executorService.shutdown();

        /*
         * Load libraries
         *
         * Some code is from
         * https://github.com/DemonWav/BukkitStart/blob/master/src/main/java/StartServer.java#L61
         */
        log.info("Loading libraries");
        URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<UCPLoader> ucpLoaderClass = ((ClassWrapper<UCPLoader>) Reflect
                .getClass("sun.misc.URLClassPath$Loader").get()).getWrappedClass();
        ClassWrapper<URLClassLoader> uclWrapper = Reflect.wrapInstance(loader);
        URLClassPath ucp = uclWrapper.getField("ucp", URLClassPath.class).get().read();
        ClassWrapper<URLClassPath> ucpWrapper = Reflect.wrapInstance(ucp);

        /* Try to get loaders map */
        Map<String, UCPLoader> lmap = null;
        try {
            lmap = (HashMap<String, UCPLoader>) ucpWrapper.getField("lmap", HashMap.class).get().read();
        } catch (NoSuchFieldException e){
            try {
                /* IBM JVM-specific I guess */
                lmap = (Map<String, UCPLoader>) ucpWrapper.getField("lmap", Map.class).get().read();
            } catch (NoSuchFieldException e2) {
                e2.addSuppressed(e);
                SneakyThrow.throwException(e2);
            }
        }
        ArrayList<UCPLoader> loaders = (ArrayList<UCPLoader>) ucpWrapper.getField("loaders", ArrayList.class).get().read();

        /* Add urls */
        final Map<String, UCPLoader> _lmap = lmap;
        libraries.forEach(url -> {
            try {
                ucpWrapper.invokeMethod("addURL", void.class, TypeWrapper.of(url));
                UCPLoader ldr = ucpWrapper.invokeMethod("getLoader", ucpLoaderClass, TypeWrapper.of(url));
                loaders.add(ldr);
                _lmap.put("file://" + url.getFile(), ldr);
            } catch (Exception e){
                SneakyThrow.throwException(e);
            }
        });

        if(System.getProperties().getProperty("java.vendor").contains("Oracle")) { /* Oracle JVM-specific */
            /* Re-enable lookup cache (the addURL will disable it) */
            ucpWrapper.getField("lookupCacheEnabled", boolean.class).get().write(true);

            /* Force cache repopulation */
            ucpWrapper.getField("lookupCacheURLs", URL[].class).get().write(null);
            ucpWrapper.getField("lookupCacheLoader", ClassLoader.class).get().write(null);
        }

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

    @SneakyThrows(MalformedURLException.class)
    private static URL convertPath(Path path) {
        return path.toUri().toURL();
    }
}
