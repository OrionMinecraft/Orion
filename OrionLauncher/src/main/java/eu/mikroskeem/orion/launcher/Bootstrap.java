/*
 * This file is part of project Orion, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 Mark Vainomaa <mikroskeem@mikroskeem.eu>
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

import eu.mikroskeem.orion.core.OrionTweakClass.OrionTweakerData;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.picomaven.DownloaderCallbacks;
import eu.mikroskeem.picomaven.PicoMaven;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import eu.mikroskeem.shuriken.reflect.ClassWrapper;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * Orion Bootstrap
 *
 * @author Mark Vainomaa
 */
public final class Bootstrap {
    final static String LIBRARIES_PATH = System.getProperty("orion.librariesPath", "./libraries");
    final static String PAPER_SERVER_JAR = System.getProperty("orion.patchedJarPath", "./cache/patched_1.12.2.jar");
    final static String PAPERCLIP_JAR = System.getProperty("orion.paperclipJarPath", "./paperclip.jar");
    final static String PAPERCLIP_URL = System.getProperty("orion.paperclipDownloadUrl",
            "https://ci.destroystokyo.com/job/PaperSpigot/lastSuccessfulBuild/artifact/paperclip.jar");

    public static void main(String... args) throws Exception {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoaderTools.URLClassLoaderTools uclTool;
        if(cl instanceof URLClassLoader)
            uclTool = new ClassLoaderTools.URLClassLoaderTools((URLClassLoader) cl);
        else
            uclTool = new ClassLoaderTools.URLClassLoaderTools(cl);

        /* Maven repositories */
        List<URI> repositories = Arrays.asList(
                URI.create("https://repo.maven.apache.org/maven2"),                     /* Central */
                URI.create("https://repo.wut.ee/repository/mikroskeem-repo"),           /* Own repository */
                URI.create("https://repo.spongepowered.org/maven"),                     /* SpongePowered repository */
                URI.create("https://oss.sonatype.org/content/groups/public"),           /* OSS Sonatype */
                URI.create("http://jcenter.bintray.com")
        );

        /* Download dependencies, TODO: do not hardcode */
        List<String> dependencies = Arrays.asList(
                "net.minecraft:launchwrapper:1.14-mikroskeem",
                "org.spongepowered:mixin:0.7.3-SNAPSHOT",
                "eu.mikroskeem:orion.at:0.0.1-SNAPSHOT",

                /* Dependency injection */
                "aopalliance:aopalliance:1.0",
                "com.google.inject:guice:4.1.0",
                "javax.inject:javax.inject:1",

                /* Configurate */
                "ninja.leaping.configurate:configurate-core:3.3",
                "ninja.leaping.configurate:configurate-hocon:3.3",
                "com.typesafe:config:1.3.1"
        );

        PicoMaven.Builder picoMavenBuilder = new PicoMaven.Builder()
                .withDownloadPath(OrionTweakerData.librariesPath = Paths.get(LIBRARIES_PATH))
                .withRepositories(repositories)
                .withDependencies(dependencies.stream().map(Dependency::fromGradle).collect(Collectors.toList()))
                .withExecutorService(Executors.newWorkStealingPool())
                .shouldCloseExecutorService(true)
                .withDownloaderCallbacks(new DownloaderCallbacks() {
                    @Override
                    public void onSuccess(Dependency dependency, Path dependencyPath) {
                        System.out.format("%s download succeeded!%n", dependency);
                    }

                    @Override
                    public void onFailure(Dependency dependency, IOException exception) {
                        System.out.format("%s download failed! %s%n", dependency, exception.getMessage());
                    }
                });

        System.out.println("Setting up Orion dependencies...");
        try(PicoMaven picoMaven = picoMavenBuilder.build()) {
            List<Path> downloadedLibraries = picoMaven.downloadAll();
            Ensure.ensureCondition(downloadedLibraries.size() == dependencies.size(),
                    "Could not download all dependencies!");

            downloadedLibraries.stream().map(ToURL::to).forEach(uclTool::addURL);
        }
        uclTool.resetCache();

        /* Set up Paperclip manager */
        PaperclipManager paperclipManager = new PaperclipManager(
                new URL(PAPERCLIP_URL),
                Paths.get(PAPERCLIP_JAR),
                Paths.get(PAPER_SERVER_JAR)
        );

        /* Set up Paper server */
        if(!paperclipManager.isServerAvailable())
            paperclipManager.invoke();

        /* Set up classpath, Orion Tweaker & launch arguments */
        paperclipManager.setupServer();

        /* Do tricks with command line arguments */
        List<String> arguments = Arrays.asList(args);
        List<String> tweakArgs = new ArrayList<>();
        tweakArgs.addAll(arguments);
        tweakArgs.add("--tweakClass");
        tweakArgs.add("eu.mikroskeem.orion.core.OrionTweakClass");

        /* Pass original arguments to tweak class */
        OrionTweakerData.originalArguments.addAll(arguments);

        /* Launch LegacyLauncher */
        ClassWrapper<?> launch = Reflect.getClass("net.minecraft.launchwrapper.Launch").orElseThrow(() ->
                        new RuntimeException("Failed to load LegacyLauncher. Please delete libraries " +
                                "folder and launch Orion again."));
        launch.invokeMethod(
                "main", void.class,
                TypeWrapper.of(String[].class, tweakArgs.toArray(new String[tweakArgs.size()]))
        );
    }
}
