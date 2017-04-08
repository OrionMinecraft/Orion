package eu.mikroskeem.orion.launcher.util;

import eu.mikroskeem.shuriken.common.streams.ByteArrays;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple artifact manager which downloads external dependencies.
 * Based on Glowstone LibraryManager util
 *
 * @author Mark Vainomaa
 */
@Slf4j
public final class LibraryManager {
    private final static AtomicInteger THREAD_COUNTER = new AtomicInteger(0);
    @Getter private final List<Library> libraries = new ArrayList<>();
    @Getter private final List<Library> runtimeLibraries = new ArrayList<>();
    private final OkHttpClient httpClient;
    private final List<String> repositories;
    private final File directory;
    private final ExecutorService downloaderService;

    public LibraryManager() {
        httpClient = new OkHttpClient();
        repositories = Arrays.asList(
                "https://repo.maven.apache.org/maven2/",                 /* Central */
                "https://repo.wut.ee/repository/mikroskeem-repo/",           /* Own repository */
                "http://ci.emc.gs/nexus/content/groups/aikar/",              /* aikar's repository */
                "https://repo.techcable.net/content/repositories/releases/", /* Techcable's repository */
                "https://repo.spongepowered.org/maven/",                     /* SpongePowered repository */
                "http://dl.bintray.com/nitram509/jbrotli/"                   /* Brotli library repository */
        );
        downloaderService = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Library downloader thread " + THREAD_COUNTER.getAndIncrement());
            return thread;
        });
        directory = new File("libraries");
    }

    public void run() {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            log.error("Could not create libraries directory: {}", directory);
        }
        for (Library library : libraries) downloaderService.execute(new LibraryDownloader(library));
        for (Library library : runtimeLibraries) downloaderService.execute(new LibraryDownloader(library));
        downloaderService.shutdown();
        try {
            downloaderService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Library Manager thread interrupted: {}" + e);
        }
    }

    /* Library download list management */
    public void addLibrary(Library library){
        if(library instanceof RuntimeLibrary) {
            runtimeLibraries.add(library);
        } else {
            libraries.add(library);
        }
    }

    public void addAllLibraries(Collection<Library> libraryCollection){
        libraryCollection.forEach(this::addLibrary);
    }

    public void removeLibrary(Library library){
        if(library instanceof RuntimeLibrary) {
            runtimeLibraries.remove(library);
        } else {
            libraries.remove(library);
        }
    }

    /* Library object */
    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString(exclude = {"checksum", "rootPath", "mavenExtraData"})
    public static class Library {
        private final String group;
        private final String artifact;
        private final String version;
        private final String checksum;
        private MavenExtraData mavenExtraData = null;
        protected Path rootPath;

        public Library(String group, String artifact, String version, String checksum, MavenExtraData mavenExtraData){
            this.group = group;
            this.artifact = artifact;
            this.version = version;
            this.checksum = checksum;
            this.mavenExtraData = mavenExtraData;
        }

        /* Library directory structure utilities */
        private Path getLibraryPath() {
            List<String> paths = new ArrayList<>();
            paths.addAll(Arrays.asList(group.split("\\.")));
            paths.add(artifact);
            paths.add(version);
            if(mavenExtraData != null) {
                paths.add(String.format(
                        "%s-%s-%s-%s.jar",
                        artifact,
                        version.replaceAll("-SNAPSHOT", ""),
                        mavenExtraData.getTimestamp(),
                        mavenExtraData.getBuildNumber()
                ));
            } else {
                paths.add(String.format("%s-%s.jar", artifact, version));
            }
            return Paths.get("", paths.toArray(new String[0]));
        }

        public Path getLocalPath() {
            return Paths.get(rootPath.toString(), getLibraryPath().toString());
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class RuntimeLibrary extends Library {
        public RuntimeLibrary(String group, String artifact, String version, String checksum) {
            super(group, artifact, version, checksum);
        }
        public RuntimeLibrary(String group, String artifact, String version, String checksum, MavenExtraData mavenExtraData) {
            super(group, artifact, version, checksum, mavenExtraData);
        }
    }

    /* Library downloader */
    @RequiredArgsConstructor
    private class LibraryDownloader implements Runnable {
        private final Library library;

        @Override
        @SneakyThrows(IOException.class)
        public void run() {
            library.rootPath = directory.getAbsoluteFile().toPath();

            /* Check if file is already downloaded and checksumFile it */
            boolean download = true;
            Path libraryPath = library.getLocalPath();
            if(Files.exists(libraryPath)) {
                if(checksumFile(libraryPath, library.getChecksum())) {
                    download = false;
                }
            }

            /* Download file */
            if(download) {
                log.info("Downloading library {}", library.toString());

                Files.createDirectories(libraryPath.getParent());
                for (String repository : repositories) {
                    URL url;
                    try {
                        url = new URL(repository + library.getLibraryPath().toString());
                    } catch (MalformedURLException e){
                        log.warn("Malformed URL: {}", e);
                        continue;
                    }
                    log.info("Trying to download from {}", url);
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", "Mozilla/5.0")
                            .url(url)
                            .get()
                            .build();

                    try(Response response = httpClient.newCall(request).execute()) {
                        if(!response.isSuccessful()) continue;
                        InputStream downloadStream = response.body().byteStream();
                        Files.copy(downloadStream, libraryPath);
                        downloadStream.close();
                    }
                    checksumFile(libraryPath, library.getChecksum());
                    break;
                }
                /* Check */
                if(!Files.exists(libraryPath)) {
                    log.error("Failed to download library {}!", library.toString());
                }
            }
        }
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class MavenExtraData {
        private final String timestamp;
        private final int buildNumber;
    }

    /* Checksum utilities */
    private boolean checksumFile(Path path, String checksum) {
        if(checksum == null) {
            log.warn("No checksum for file {}, assuming file is valid", path.getFileName());
            return true;
        }
        return getSha1sum(path.toFile()).equals(checksum);
    }

    @SneakyThrows
    private String getSha1sum(File file){
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream fis = new FileInputStream(file)) {
            sha1.update(ByteArrays.fromInputStream(fis));
        }
        return new HexBinaryAdapter().marshal(sha1.digest());
    }
}
