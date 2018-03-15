/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.orion.launcher;

import eu.mikroskeem.orion.core.launcher.AbstractLauncherService;
import eu.mikroskeem.orion.core.launcher.BlackboardKey;
import eu.mikroskeem.orion.core.launcher.legacylauncher.LegacyLauncherService;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.picomaven.DownloaderCallbacks;
import eu.mikroskeem.picomaven.PicoMaven;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import net.minecraft.launchwrapper.Launch;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;


/**
 * Orion Bootstrap
 *
 * @author Mark Vainomaa
 */
public final class Bootstrap {
    /** Orion properties */
    private final static Properties orionProperties = new Properties();

    static {
        Path propertiesPath = Paths.get(System.getProperty("orion.propertiesPath"), "./orion.properties");
        if(Files.exists(propertiesPath) && Files.isRegularFile(propertiesPath)) {
            try {
                orionProperties.load(Files.newBufferedReader(propertiesPath));
            } catch (IOException e) {
                System.out.println("Failed to load properties from " + propertiesPath);
                e.printStackTrace();
            }
        }
    }

    @NotNull
    private static String getProperty(@NotNull String key, @NotNull String def) {
        String value = orionProperties.getProperty(key, System.getProperty(key, def));
        return value.isEmpty() ? def : value;
    }

    private static boolean getBoolean(@NotNull String key) {
        boolean result = false;
        try {
            result = Boolean.parseBoolean(orionProperties.getProperty(key, System.getProperty(key)));
        } catch (IllegalArgumentException | NullPointerException ignored) {}
        return result;
    }

    /** Preload libraries path - used to add libraries to classpath before loading server jar */
    private final static Path PRELOAD_LIBRARIES_PATH = Paths.get(getProperty("orion.preloadLibrariesPath", "./preload_libraries"));

    /** Allows loading jars from {@link Bootstrap#PRELOAD_LIBRARIES_PATH} */
    private final static boolean PRELOAD_ALLOWED = getBoolean("orion.allowPreloadLibraries");

    /** Makes {@link Bootstrap} not append Orion {@link eu.mikroskeem.orion.core.OrionTweakClass} argument to LegacyLauncher */
    private final static boolean DONT_APPEND_TWEAK_CLASS_ARGUMENT = getBoolean("orion.dontAppendTweakClassArgument");

    /** Runtime libraries directory */
    private final static Path LIBRARIES_PATH = Paths.get(getProperty("orion.librariesPath", "./libraries"));

    /** Specifies Paper server jar path */
    private final static Path PAPER_SERVER_JAR = Paths.get(getProperty("orion.patchedJarPath", "./cache/patched_1.12.2.jar"));

    /** Specifies Paperclip jar path */
    private final static Path PAPERCLIP_JAR = Paths.get(getProperty("orion.paperclipJarPath", "./paperclip.jar"));

    /** Whether to check for server jar only or check for both (default behaviour). Last one triggers paperclip invocation if paperclip's jar is missing */
    private final static boolean CHECK_FOR_SERVER_JAR_INSTEAD = getBoolean("orion.checkForServerJarInstead");

    /** Path, where Orion should look up mod jars */
    private final static Path MODS_PATH = Paths.get(getProperty("orion.modsPath", "./mods"));

    /** Path, where per-mod configurations are stored */
    private final static Path MOD_CONFIGS_PATH = Paths.get(getProperty("orion.modconfigsPath", "./modconfigs"));

    /** Paperclip download URL */
    private final static String PAPERCLIP_URL = getProperty("orion.paperclipDownloadUrl",
            "https://ci.destroystokyo.com/job/PaperSpigot/lastSuccessfulBuild/artifact/paperclip.jar");

    public static void main(String... args) throws Exception {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoaderTools.URLClassLoaderTools uclTool = new ClassLoaderTools.URLClassLoaderTools(cl);
        OkHttpClient httpClient = new OkHttpClient();

        /* Load preload libraries, if allowed */
        if(PRELOAD_ALLOWED) {
            if(Files.notExists(PRELOAD_LIBRARIES_PATH) || !Files.isDirectory(PRELOAD_LIBRARIES_PATH)) {
                Files.deleteIfExists(PRELOAD_LIBRARIES_PATH);
                Files.createDirectories(PRELOAD_LIBRARIES_PATH);
            }
            try(Stream<Path> file = Files.walk(PRELOAD_LIBRARIES_PATH)) {
                file.filter(Files::isRegularFile)
                        .filter(f -> f.toString().endsWith(".jar"))
                        .peek(f -> System.out.println("Preloading library: " + f))
                        .map(ToURL::to)
                        .forEach(uclTool::addURL);
            }
        }

        /* Set up Paperclip manager */
        PaperclipManager paperclipManager = new PaperclipManager(
                new URL(PAPERCLIP_URL), PAPERCLIP_JAR,
                PAPER_SERVER_JAR, uclTool, httpClient);

        /* Set up Paper server */
        if(CHECK_FOR_SERVER_JAR_INSTEAD) {
            if(!paperclipManager.isServerAvailable())
                paperclipManager.invoke();
        } else {
            if(Files.notExists(PAPERCLIP_JAR) || !paperclipManager.isServerAvailable())
                paperclipManager.invoke();
        }

        /* Set up classpath, Orion Tweaker & launch arguments */
        String launchTarget = paperclipManager.setupServer();

        /* Logger can be set up now */
        Logger log = LogManager.getLogger("OrionBootstrap");
        try(InputStream is = requireNonNull(Bootstrap.class.getResourceAsStream("/orion-version.properties"))) {
            Properties ver = new Properties(); ver.load(is);
            log.info("Orion Launcher version {} (git: {}/{})",
                    ver.getProperty("version"),
                    ver.getProperty("gitBranch"),
                    ver.getProperty("gitCommitId")
            );
            new GithubChecker(log, ver, httpClient).check();
        } catch (Exception e) {
            log.info("Orion Launcher");
            log.warn("Failed to obtain version information from jar", e);
        }

        /* Maven repositories */
        List<URI> repositories = Arrays.asList(
                URI.create("https://repo.maven.apache.org/maven2"),                     /* Central */
                URI.create("https://repo.wut.ee/repository/mikroskeem-repo"),           /* Own repository */
                URI.create("https://repo.spongepowered.org/maven"),                     /* SpongePowered repository */
                URI.create("https://oss.sonatype.org/content/groups/public"),           /* OSS Sonatype */
                URI.create("http://jcenter.bintray.com")
        );

        /* Download dependencies */
        List<Dependency> dependencies = new ArrayList<>();
        try(BufferedReader depsReader = new BufferedReader(
                new InputStreamReader(Bootstrap.class.getClassLoader().getResourceAsStream("deps.txt")))) {
            String line;
            while((line = depsReader.readLine()) != null) {
                log.debug("Required dependency: {}", line);
                dependencies.add(Dependency.fromGradle(line));
            }
        }

        PicoMaven.Builder picoMavenBuilder = new PicoMaven.Builder()
                .withOkHttpClient(httpClient)
                .withDownloadPath(LIBRARIES_PATH)
                .withRepositories(repositories)
                .withDependencies(dependencies)
                .withExecutorService(Executors.newWorkStealingPool())
                .shouldCloseExecutorService(true)
                .withDebugLoggerImpl((format, contents) -> log.debug(format.replace("%s", "{}"), contents))
                .withDownloaderCallbacks(new DownloaderCallbacks() {
                    @Override
                    public void onSuccess(Dependency dependency, Path dependencyPath) {
                        log.info("{} download succeeded!", dependency);
                    }

                    @Override
                    public void onFailure(Dependency dependency, Exception exception) {
                        log.warn("{} download failed! {}", dependency, exception);
                    }
                });

        log.info("Setting up Orion dependencies...");
        try(PicoMaven picoMaven = picoMavenBuilder.build()) {
            List<Path> downloadedLibraries = picoMaven.downloadAll();
            Ensure.ensureCondition(downloadedLibraries.size() == dependencies.size(),
                    "Could not download all dependencies!");

            downloadedLibraries.stream().map(ToURL::to).forEach(uclTool::addURL);
        }
        uclTool.resetCache();

        /* Do tricks with command line arguments */
        List<String> arguments = Arrays.asList(args);
        List<String> tweakArgs = new ArrayList<>(arguments);

        if(!DONT_APPEND_TWEAK_CLASS_ARGUMENT) {
            tweakArgs.add("--tweakClass");
            tweakArgs.add("eu.mikroskeem.orion.core.OrionTweakClass");
        }

        /* Set up LegacyLauncher */
        log.debug("Setting up LegacyLauncher platform");
        BlackboardKey.getOr(BlackboardKey.LAUNCHER_SERVICE, () -> {
            AbstractLauncherService launcherService = new LegacyLauncherService();
            BlackboardKey.blackboard = launcherService.getBlackBoard();
            return launcherService;
        });

        /* Populate blackboard */
        BlackboardKey.set(BlackboardKey.ORIGINAL_ARGUMENTS, Collections.unmodifiableList(arguments));
        BlackboardKey.set(BlackboardKey.LAUNCH_TARGET, launchTarget);
        BlackboardKey.set(BlackboardKey.MODS_PATH, MODS_PATH);
        BlackboardKey.set(BlackboardKey.MOD_CONFIGS_PATH, MOD_CONFIGS_PATH);
        BlackboardKey.set(BlackboardKey.LIBRARIES_PATH, LIBRARIES_PATH);

        /* Launch LegacyLauncher */
        log.info("Starting LegacyLauncher with arguments {}", tweakArgs);
        Launch.main(tweakArgs.toArray(new String[0]));
    }
}
