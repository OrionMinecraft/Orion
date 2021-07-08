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

import org.checkerframework.checker.nullness.qual.NonNull;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;


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
    private final ClassLoader classLoader;

    PaperclipManager(@NonNull URL paperclipDownloadUrl, @NonNull Path paperclipPath,
                     @NonNull Path serverPath, ClassLoader classLoader) {
        this.oldSecurityManager = System.getSecurityManager();
        this.paperclipPath = paperclipPath;
        this.serverPath = serverPath;
        this.paperclipDownloadUrl = paperclipDownloadUrl;
        this.classLoader = classLoader;
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
    @NonNull
    String setupServer() {
        if(!isServerAvailable()) throw new IllegalStateException("Paper server jar is not available! Check if '" + serverPath + "' is available.");

        /* Load server jar to system classloader */
        Utils.addURLToClassLoader(classLoader, serverPath);

        /* Return server jar main class */
        return requireNonNull(Utils.getMainClassFromJar(serverPath), "Failed to get server main class!");
    }

    /**
     * Invokes Paperclip
     */
    void invoke() {
        /* Check if paperclip jar is present */
        if(Files.notExists(paperclipPath)) {
            System.out.println("Downloading Paperclip...");

            try {
                HttpURLConnection connection =
                        (HttpURLConnection) paperclipDownloadUrl.openConnection();

                connection.addRequestProperty("User-Agent", "OrionMinecraft");

                if (connection.getResponseCode() != 200) {
                    throw new IOException(String.format("HTTP request to %s returned %s!%n",
                            paperclipDownloadUrl, connection.getResponseMessage()));
                }

                try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
                    try (OutputStream output = new FileOutputStream(paperclipPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = input.read(buffer)) != -1) {
                            output.write(buffer, 0, count);
                        }
                    }
                }

                connection.disconnect();
            } catch (IOException e) {
                throw new RuntimeException("Failed to download Paperclip, either download it yourself or try again later.", e);
            }
        }

        /* Invoke paperclip */
        System.out.println("Executing Paperclip... please wait");
        try {
            System.setSecurityManager(new PaperclipExitCatcher());
            URLClassLoader ucl = new URLClassLoader(new URL[]{paperclipPath.toUri().toURL()});
            String paperclipTargetClass = Objects.requireNonNull(Utils.getMainClassFromJar(paperclipPath), "Failed to get Paperclip Main-Class from its manifest!");

            System.setProperty("paperclip.patchonly", "true");

            try {
                Class<?> clazz = Class.forName(paperclipTargetClass, true, ucl);

                Method method = clazz.getDeclaredMethod("main", String[].class);
                method.invoke(null, new String[0]);
            } catch (ClassNotFoundException exception) {
                throw new RuntimeException("Failed to get class " + paperclipTargetClass);
            }
        } catch (Throwable e) {
            if(!(e instanceof InvocationTargetException)) {
                throw new RuntimeException(e);
            }

            Throwable target = ((InvocationTargetException) e).getTargetException();
            if(!(target instanceof PaperclipExitException)) {
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
        public void checkPermission(@NonNull Permission perm) {
            /* Check only exitVM, as this security manager needs to catch only System.exit(int) */
            if(!perm.getName().startsWith("exitVM"))
                return;
            int exitCode = Integer.parseInt(perm.getName().split(Pattern.quote("."))[1]);
            throw new PaperclipExitException(exitCode);
        }
    }
}
