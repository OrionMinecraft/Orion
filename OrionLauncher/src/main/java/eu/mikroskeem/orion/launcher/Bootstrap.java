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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.mikroskeem.orion.core.OrionTweakClass.OrionTweakerData;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.picomaven.DownloaderCallbacks;
import eu.mikroskeem.picomaven.PicoMaven;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import net.minecraft.launchwrapper.Launch;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

import static java.util.Objects.requireNonNull;


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

        /* Set up Paperclip manager */
        PaperclipManager paperclipManager = new PaperclipManager(
                new URL(PAPERCLIP_URL), Paths.get(PAPERCLIP_JAR),
                Paths.get(PAPER_SERVER_JAR), uclTool);

        /* Set up Paper server */
        if(!paperclipManager.isServerAvailable())
            paperclipManager.invoke();

        /* Set up classpath, Orion Tweaker & launch arguments */
        paperclipManager.setupServer();

        /* Logger can be set up now */
        Logger log = LogManager.getLogger("OrionBootstrap");
        try(InputStream is = requireNonNull(Bootstrap.class.getResourceAsStream("/orion-version.properties"))) {
            Properties ver = new Properties(); ver.load(is);
            log.info("Orion Launcher version {} (git: {}/{})",
                    ver.getProperty("version"),
                    ver.getProperty("gitBranch"),
                    ver.getProperty("gitCommitId")
            );
            new Thread(() -> {
                OkHttpClient client = new OkHttpClient();
                HttpUrl url = HttpUrl.parse("https://api.github.com/repos/" +
                        ver.getProperty("gitRepository") +
                        "/compare/" +
                        ver.getProperty("gitBranch") +
                        "..." +
                        ver.getProperty("gitCommitId"));
                Request request = new Request.Builder()
                        .get()
                        .url(requireNonNull(url))
                        .build();
                try(Response response = client.newCall(request).execute()) {
                    if(response.isSuccessful()) {
                        JsonObject root = new JsonParser().parse(response.body().charStream()).getAsJsonObject();
                        int behindBy = root.get("behind_by").getAsInt();
                        if(behindBy > 0) {
                            log.info("This Orion version is up to date!");
                        } else {
                            log.info("This Orion version is behind by {} commits!", behindBy);
                        }
                    } else {
                        throw new IOException("HTTP code: " + response.code());
                    }
                } catch (IOException e) {
                    log.warn("Failed to obtain latest version info from GitHub!", e);
                }
            }).start();
        } catch (Exception e) {
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
                .withDownloadPath(OrionTweakerData.librariesPath = Paths.get(LIBRARIES_PATH))
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
                    public void onFailure(Dependency dependency, IOException exception) {
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
        List<String> tweakArgs = new ArrayList<>();
        tweakArgs.addAll(arguments);
        tweakArgs.add("--tweakClass");
        tweakArgs.add("eu.mikroskeem.orion.core.OrionTweakClass");

        /* Pass original arguments to tweak class */
        OrionTweakerData.originalArguments.addAll(arguments);

        /* Launch LegacyLauncher */
        log.info("Starting LegacyLauncher with arguments {}", tweakArgs);
        Launch.main(tweakArgs.toArray(new String[tweakArgs.size()]));
    }
}
