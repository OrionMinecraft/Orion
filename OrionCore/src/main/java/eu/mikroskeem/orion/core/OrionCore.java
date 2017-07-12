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

package eu.mikroskeem.orion.core;

import com.google.common.eventbus.EventBus;
import eu.mikroskeem.orion.api.OrionAPI;
import eu.mikroskeem.orion.api.events.ModLoadEvent;
import eu.mikroskeem.orion.core.mod.ModClassVisitor;
import eu.mikroskeem.orion.core.mod.ModContainer;
import eu.mikroskeem.orion.core.mod.ModInfo;
import eu.mikroskeem.picomaven.Dependency;
import eu.mikroskeem.picomaven.DownloaderCallbacks;
import eu.mikroskeem.picomaven.PicoMaven;
import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.common.data.Pair;
import eu.mikroskeem.shuriken.common.streams.ByteArrays;
import eu.mikroskeem.shuriken.injector.Binder;
import eu.mikroskeem.shuriken.injector.Injector;
import eu.mikroskeem.shuriken.injector.ShurikenInjector;
import eu.mikroskeem.shuriken.reflect.ClassWrapper;
import eu.mikroskeem.shuriken.reflect.FieldWrapper;
import eu.mikroskeem.shuriken.reflect.Reflect;
import net.minecraft.launchwrapper.LaunchClassLoader;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * The mighty Orion Core
 *
 * @author Mark Vainomaa
 */
public final class OrionCore {
    private static final Logger logger = LogManager.getLogger("OrionCore");
    public static OrionCore INSTANCE = new OrionCore();


    final List<ModContainer<?>> mods = new ArrayList<>();
    final List<String> mixinConfigurations = new ArrayList<>();
    final List<URI> modMavenRepositories = new ArrayList<>();
    final List<Dependency> modLibraries = new ArrayList<>();

    /**
     * Private constructor to set up Mixin loader
     */
    private OrionCore() {
        logger.debug("Setting up SpongeMixin library...");
        MixinBootstrap.init();

        logger.debug("Settinng Mixin Environment side to SERVER");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.SERVER);

        logger.debug("Setting up Mixin error handler");
        Mixins.registerErrorHandlerClass(OrionMixinErrorHandler.class.getName());

        logger.debug("Mixin library initialization finished!");
    }

    /**
     * Gets mods list
     *
     * @return Loaded list
     */
    public List<ModContainer<?>> getMods() {
        return mods;
    }

    /**
     * Sets up Orion transformers
     *
     * @param launchClassLoader LegacyLauncher class loader
     */
    @Contract("null -> fail")
    void setupTransformers(LaunchClassLoader launchClassLoader) {
        /* Access transformer */
        launchClassLoader.registerTransformer(OrionAccessTransformer.class.getName());
    }

    /**
     * Sets up Orion core mixins and transformers
     *
     * @param launchClassLoader LegacyLauncher class loader
     */
    @Contract("null -> fail")
    void setupCore(LaunchClassLoader launchClassLoader) {
        logger.debug("Adding class load exclusions");
        launchClassLoader.addClassLoaderExclusion("ninja.leaping.configurate");
        launchClassLoader.addClassLoaderExclusion("org.apache.logging.log4j");
        launchClassLoader.addClassLoaderExclusion("eu.mikroskeem.orion.api");
        launchClassLoader.addClassLoaderExclusion("com.google.common");
        launchClassLoader.addClassLoaderExclusion("javax.inject");

        logger.debug("Setting up OrionAPI singleton");
        OrionAPI.setInstance(new OrionAPIImpl(this));
    }

    /**
     * Sets up Orion mods
     *
     * @param launchClassLoader LegacyLauncher class loader
     * @param modsDirectory Directory where mods should reside
     * @throws IOException If {@link Files#list(Path)} fails
     */
    @Contract("null, null -> fail")
    void setupMods(LaunchClassLoader launchClassLoader, Path modsDirectory) throws IOException {
        Set<String> modLoadOrder = new HashSet<>();
        Map<String, Pair<ModInfo, Path>> foundMods = new LinkedHashMap<>();

        logger.debug("Initializing Orion mods");
        if(Files.notExists(modsDirectory)) Files.createDirectories(modsDirectory);
        Ensure.ensureCondition(Files.isDirectory(modsDirectory), modsDirectory + " is not a directory!");

        /* Scan mods directory for plugin files */
        Files.list(modsDirectory).filter(file -> {
            if(!file.getFileName().toString().endsWith(".jar")) {
                logger.warn("Ignoring file '{}'", file);
                return false;
            }
            return true;
        }).forEach(modFile -> {
            logger.debug("Scanning mod candidate '{}' for mod classes", modFile);

            /* Scan for mod classes */
            ModInfo modInfo = null;
            try(ZipFile zipFile = new ZipFile(modFile.toFile())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while(entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    /* Skip directories */
                    if(entry.isDirectory()) continue;

                    if(entry.getName().endsWith(".class")) {
                        byte[] classData = ByteArrays.fromInputStream(zipFile.getInputStream(entry));
                        modInfo = ModClassVisitor.getModInfo(classData);
                        if(modInfo != null) {
                            logger.info("Found mod with id '{}' from '{}'", modInfo.getId(), modFile);
                            break;
                        }
                    }

                    /*
                     * Note: check for non-Orion mods is done here, because mod author *may*
                     * include plugin code in same jar (for... runtime retransformations?)
                     */
                    if(entry.getName().equals("plugin.yml")) {
                        logger.warn("Mod file '{}' seems to be actually a Bukkit plugin.", modFile);
                    }

                    if(entry.getName().equals("mcmod.info")) {
                        logger.warn("Mod file '{}' seems to be actually a Sponge or Forge mod.", modFile);
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to open jar!", e);
            }

            if(modInfo == null) {
                logger.warn("Skipping invalid mod: {}", modFile);
                return;
            }

            /* Add mod to found mods list */
            foundMods.put(modInfo.getId(), new Pair<>(modInfo, modFile));
        });

        /* Order mod loading */
        for (Pair<ModInfo, Path> modInfo : foundMods.values()) {
            String modId = modInfo.getKey().getId();
            //if(modLoadOrder.contains(modInfo.getKey().getId())) continue;

            /* Iterate over mod dependencies */
            for(String dependencyId : modInfo.getKey().getDependencies()) {
                /* People like to do dumb stuff */
                Ensure.ensureCondition(!modId.equals(dependencyId),
                        "Mod '" + dependencyId + "' cannot depend on itself!");
                Ensure.ensureCondition(foundMods.containsKey(dependencyId),
                        "Could not find dependency '" + dependencyId + "' for mod '" + modId + "'");
                modLoadOrder.add(dependencyId);
            }

            modLoadOrder.add(modId);
        }

        /* Construct mods */
        for (String modId : modLoadOrder) {
            Pair<ModInfo, Path> modInfo = foundMods.get(modId);
            URL modUrl = ToURL.to(modInfo.getValue());
            launchClassLoader.addURL(modUrl);
            ClassWrapper<?> modClass = Reflect.getClassThrows(modInfo.getKey().getClassName(), launchClassLoader);
            ModContainer<?> mod = initializeMod(modClass, modInfo.getKey());
            mod.init();
            mods.add(mod);
        }

        /* Process mod requested libraries */
        if(modLibraries.size() > 0) {
            logger.info("Downloading {} extra libraries requested by installed mods...", modLibraries.size());
            ExecutorService downloaderPool = Executors.newWorkStealingPool();
            PicoMaven.Builder picoMavenBuilder = new PicoMaven.Builder()
                    .withExecutorService(downloaderPool)
                    .shouldCloseExecutorService(false)
                    .withDownloadPath(OrionTweakClass.OrionTweakerData.librariesPath)
                    .withRepositories(modMavenRepositories)
                    .withDependencies(modLibraries)
                    .withDownloaderCallbacks(new DownloaderCallbacks() {
                        @Override
                        public void onSuccess(Dependency dependency, Path dependencyPath) {
                            logger.info("Dependency {} downloaded successfully!", dependency);
                        }

                        @Override
                        public void onFailure(Dependency dependency, IOException exception) {
                            logger.error("Failed to download dependency {}! {}", dependency, exception);
                        }
                    });

            try(PicoMaven picoMaven = picoMavenBuilder.build()) {
                List<Path> downloadedLibraries = picoMaven.downloadAll();
                Ensure.ensureCondition(downloadedLibraries.size() == modLibraries.size(),
                        "Could not download all libraries!");

                /* Add all libraries to LaunchClassLoader */
                downloadedLibraries.stream().map(ToURL::to).forEach(launchClassLoader::addURL);
            } catch (InterruptedException e) {
                logger.error("Library download interrupted!", e);
            }
        }

        /* Load mods */
        for(ModContainer<?> mod : mods) {
            mod.getEventBus().post(new ModLoadEvent());
        }
    }

    private <T> ModContainer<T> initializeMod(ClassWrapper<T> modClass, ModInfo modInfo) {
        /* Set up mod event bus */
        EventBus modEventBus = new EventBus();

        /* Set up configuration loader */
        ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
        try {
            Path configurationPath = Paths.get("./modconfigs", modInfo.getId() + ".cfg");
            if(Files.notExists(configurationPath.getParent())) Files.createDirectories(configurationPath.getParent());
            configurationLoader = HoconConfigurationLoader.builder()
                    .setPath(configurationPath)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /* Set up dependency injector */
        Injector injector = ShurikenInjector.createInjector(b -> {
            b.bind(ModInfo.class).toInstance(modInfo);
            b.bind(EventBus.class).toInstance(modEventBus);
            b.bind(Logger.class).toInstance(LogManager.getLogger(modInfo.getId()));
            b.bind(ConfigurationLoader.class).toInstance(configurationLoader);
        });

        /* TODO: make Shuriken's injector inject itself as well by default */
        Reflect.wrapInstance(injector).getField("binder", Binder.class)
                .map(FieldWrapper::read)
                .ifPresent(b -> b.bind(Injector.class).toInstance(injector));

        return new ModContainer<>(modClass, modInfo, injector, modEventBus);
    }
}
