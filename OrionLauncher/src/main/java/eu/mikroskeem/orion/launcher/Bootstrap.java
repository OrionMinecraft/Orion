/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2020 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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
import eu.mikroskeem.orion.core.launcher.legacylauncher.OrionTweakClass;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * Orion Bootstrap
 *
 * @author Mark Vainomaa
 */
public final class Bootstrap {
    /** Orion properties */
    private final static Properties orionProperties = new Properties();

    static {
        Path propertiesPath = Paths.get(System.getProperty("orion.propertiesPath", "./orion.properties"));
        if(Files.exists(propertiesPath) && Files.isRegularFile(propertiesPath)) {
            try {
                orionProperties.load(Files.newBufferedReader(propertiesPath));
                System.out.println("Loaded Orion properties from " + propertiesPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties from " + propertiesPath, e);
            }
        }
    }

    @NonNull
    private static String getProperty(@NonNull String key, @NonNull String def) {
        return getProperty(key, () -> def);
    }

    @NonNull
    private static String getProperty(@NonNull String key, @NonNull Supplier<String> def) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = orionProperties.getProperty(key);
        }
        if (value == null || value.isEmpty()) {
            value = def.get();
        }
        return value;
    }

    private static boolean getBoolean(@NonNull String key) {
        boolean result = false;
        try {
            result = Boolean.parseBoolean(orionProperties.getProperty(key, System.getProperty(key)));
        } catch (IllegalArgumentException | NullPointerException ignored) {}
        return result;
    }

    /** Minecraft version */
    private final static String MINECRAFT_VERSION = getProperty("orion.minecraftVersion", "1.14.4");

    /** Preload libraries path - used to add libraries to classpath before loading server jar */
    private final static Path PRELOAD_LIBRARIES_PATH = Paths.get(getProperty("orion.preloadLibrariesPath", "./preload_libraries"));

    /** Allows loading jars from {@link Bootstrap#PRELOAD_LIBRARIES_PATH} */
    private final static boolean PRELOAD_ALLOWED = getBoolean("orion.allowPreloadLibraries");

    /** Makes {@link Bootstrap} not append Orion {@link OrionTweakClass} argument to LegacyLauncher */
    private final static boolean DONT_APPEND_TWEAK_CLASS_ARGUMENT = getBoolean("orion.dontAppendTweakClassArgument");

    /** Runtime libraries directory */
    private final static Path LIBRARIES_PATH = Paths.get(getProperty("orion.librariesPath", "./libraries"));

    /** Specifies Paper server jar path */
    private final static Path PAPER_SERVER_JAR = Paths.get(getProperty("orion.patchedJarPath", () -> String.format("./cache/patched_%s.jar", MINECRAFT_VERSION)));

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
            () -> String.format("https://papermc.io/api/v1/paper/%s/latest/download", MINECRAFT_VERSION));

    // ** Whether to die on mod loading error or not */
    //private final static Boolean DONT_DIE_ON_MOD_LOAD_ERROR = getBoolean("orion.dontDieOnModLoadError");

    public static void main(String... args) throws Exception {
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

        /* Do tricks with command line arguments */
        List<String> arguments = Arrays.asList(args);
        List<String> tweakArgs = new ArrayList<>(arguments);

        if(!DONT_APPEND_TWEAK_CLASS_ARGUMENT) {
            tweakArgs.add("--tweakClass");
            tweakArgs.add("eu.mikroskeem.orion.core.launcher.legacylauncher.OrionTweakClass");
        }

        /* Set up LegacyLauncher */
        log.debug("Setting up LegacyLauncher platform");
        BlackboardKey.getOr(BlackboardKey.LAUNCHER_SERVICE, () -> {
            AbstractLauncherService launcherService = new LegacyLauncherService();
            BlackboardKey.setBlackboard(launcherService.getBlackBoard());
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
