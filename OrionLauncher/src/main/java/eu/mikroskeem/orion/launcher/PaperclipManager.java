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
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.instrumentation.ClassLoaderTools;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.regex.Pattern;


/**
 * Manages Paperclip to get patched server jar
 *
 * @author Mark Vainomaa
 */
final class PaperclipManager {
    private final SecurityManager oldSecurityManager;
    private final URL paperclipDownloadUrl;
    private final Path paperclipPath;
    private final Path serverPath;
    private final ClassLoaderTools.URLClassLoaderTools uclTools;

    PaperclipManager(URL paperclipDownloadUrl, Path paperclipPath, Path serverPath,
                     ClassLoaderTools.URLClassLoaderTools uclTools) {
        this.oldSecurityManager = System.getSecurityManager();
        this.paperclipPath = paperclipPath;
        this.serverPath = serverPath;
        this.paperclipDownloadUrl = paperclipDownloadUrl;
        this.uclTools = uclTools;
    }

    /**
     * Checks if server is available
     *
     * @return Whether server is available or not
     */
    boolean isServerAvailable() {
        return Files.exists(serverPath);
    }

    /**
     * Sets up Orion Tweaker
     */
    void setupServer() {
        if(!isServerAvailable()) throw new IllegalStateException("Paper server jar is not available!");
        OrionTweakerData.launchTarget = Utils.getMainClassFromJar(serverPath);

        /* Load server jar to system classloader */
        uclTools.addURL(ToURL.to(serverPath));
    }

    /**
     * Invokes Paperclip
     */
    void invoke() {
        /* Check if paperclip jar is present */
        if(Files.notExists(paperclipPath)) {
            System.out.println("Downloading Paperclip...");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(paperclipDownloadUrl)
                    .get()
                    .build();
            try(Response response = client.newCall(request).execute()) {
                if(response.code() != 200)
                    throw new IOException(String.format("HTTP request to %s returned %s!%n",
                            paperclipDownloadUrl, response.cacheControl()));
                Files.createDirectories(paperclipPath.getParent());
                Files.copy(response.body().byteStream(), paperclipPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /* Invoke paperclip */
        System.out.println("Executing Paperclip... please wait");
        try {
            System.setSecurityManager(new PaperclipExitCatcher());
            URLClassLoader ucl = new URLClassLoader(new URL[]{ToURL.to(paperclipPath)});
            String paperclipTargetClass = Utils.getMainClassFromJar(paperclipPath);

            System.setProperty("paperclip.patchonly", "true");
            Reflect.getClass(paperclipTargetClass, ucl)
                    .orElseThrow(() -> new RuntimeException("Failed to get class " + paperclipTargetClass))
                    .invokeMethod("main", void.class, TypeWrapper.of(new String[0]));
        } catch (Throwable e) {
            if(!(e instanceof InvocationTargetException)) {
                throw new RuntimeException(e);
            }

            Throwable target = ((InvocationTargetException) e).getTargetException();
            if(target == null || !(target instanceof PaperclipExitException)) {
                throw new RuntimeException(e);
            }

            int exitCode = ((PaperclipExitException) target).exitCode;
            if(exitCode != 0)
                throw new RuntimeException("Paperclip exited with code " + exitCode);
        }
        System.getProperties().remove("paperclip.patchonly");
        System.setSecurityManager(oldSecurityManager);
    }

    /** Exception to catch {@link System#exit(int)} in Paperclip */
    class PaperclipExitException extends SecurityException {
        final int exitCode;

        PaperclipExitException(int exitCode) {
            this.exitCode = exitCode;
        }
    }

    /** SecurityManager to catch Paperclip exit */
    class PaperclipExitCatcher extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            /* Check only exitVM, as this security manager needs to catch only System.exit(int) */
            if(!perm.getName().startsWith("exitVM"))
                return;
            int exitCode = Integer.parseInt(perm.getName().split(Pattern.quote("."))[1]);
            throw new PaperclipExitException(exitCode);
        }
    }
}
