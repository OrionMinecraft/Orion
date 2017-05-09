package eu.mikroskeem.orion.mod;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import eu.mikroskeem.orion.api.mod.OrionMod;
import eu.mikroskeem.orion.api.mod.OrionModContainer;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.ClassWrapper;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

import static eu.mikroskeem.shuriken.common.ToURL.to;

/**
 * Orion Tweak class to load Mixins for CraftBukkit-derivatives
 *
 * @author Mark Vainomaa
 */
@Slf4j
public class OrionTweakClass implements ITweaker {
    public static String launchTarget;
    @Getter private String[] launchArguments;
    public Set<OrionModContainer> loadedMods = new HashSet<>();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.launchArguments = args.toArray(new String[0]);

        /*
         * Fix ze logging
         * Credits to Minecrell & kashike
         */
        if (System.getProperty("log4j.configurationFile") == null) {
            System.setProperty("log4j.configurationFile", "orion_log4j2.xml");
            ((LoggerContext) LogManager.getContext(false)).reconfigure();
        }
    }

    @Override
    public String getLaunchTarget() {
        return OrionTweakClass.launchTarget;
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        /* Classloader exclusions */
        String[] loadExclusions = new String[] {
                "com.mojang.util.QueueLogAppender",     /* Mojang's Log4j2 plugin */
        };
        for (String exclusion : loadExclusions) launchClassLoader.addClassLoaderExclusion(exclusion);

        /* Set up mixins and transformers */
        setupMixins();
        //setupTransformers(launchClassLoader);

        /* Set up mods */
        loadMods(launchClassLoader);
    }

    private void setupMixins(){
        log.info("Setting up Orion mixins...");
        /* Set up mixin framework */
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.SERVER);

        /* Mixins */
        Mixins.addConfiguration("orion.mixins.json");
        Mixins.addConfiguration("orion.spawnpoint.mixins.json");
        Mixins.addConfiguration("orion.player.mixins.json");

        /* Ready */
        log.info("Done!");
    }

    private void setupTransformers(LaunchClassLoader launchClassLoader) {
        log.info("Setting up transformers...");
        List<String> transformers = Arrays.asList(
        );
        transformers.forEach(launchClassLoader::registerTransformer);

        /* Ready */
        log.info("Done!");
    }

    @SneakyThrows(IOException.class)
    private void loadMods(LaunchClassLoader launchClassLoader) {
        log.info("Loading mods...");
        Path modsPath = Paths.get("./mods");
        Files.createDirectories(modsPath);
        /* Count failures */
        AtomicInteger failures = new AtomicInteger(0);

        /* List files in mods directory */
        Files.list(modsPath).filter(p -> p.getFileName().toString().endsWith(".jar")).forEach(modJar -> {
            /* Check if mod file is valid jar */
            log.info("Found mod candidate {}", modJar.getFileName());
            try(JarFile j = new JarFile(modJar.toFile())) {
                j.getComment();
            } catch (IOException e) {
                log.error("Invalid mod " + modJar.getFileName() +": " + e.getMessage(), e);
                return;
            }

            /* Scan for annotated mod classes */
            URL url = to(modJar);
            FastClasspathScanner fpc = new FastClasspathScanner().overrideClasspath(url);
            ScanResult scanResult = fpc.scan();
            List<String> modClasses = scanResult.getNamesOfClassesWithAnnotation(OrionMod.class);
            if(modClasses.size() != 1) {
                log.error("Invalid mod file {}, no class annotated with OrionMod found.", modJar.getFileName());
                return;
            }

            /* Load mod into classloader */
            launchClassLoader.addURL(url);
            ClassWrapper<?> modClass = Reflect.getClass(modClasses.get(0), launchClassLoader).get();
            OrionMod modInfo = modClass.getWrappedClass().getAnnotation(OrionMod.class);
            Version modVersion;

            /* Parse mod information */
            try {
                modVersion = Version.valueOf(modInfo.version());
            } catch (ParseException e) {
                log.error("Mod " + modInfo.name() + "has invalid version string (" + modInfo.version() + "): " + e.getMessage(), e);
                failures.incrementAndGet();
                return;
            }

            /* Wrap mod into container */
            OrionModContainer modContainer = new OrionModContainer(
                    modClass.getWrappedClass(),
                    new OrionModContainer.ModInfo(modVersion, Arrays.asList(modInfo.authors()))
            );

            /* Load mod mixins */
            for (String mixinFile : modInfo.mixins()) {
                if(mixinFile.startsWith("mixins." + modInfo.name()) && mixinFile.endsWith(".json")) {
                    Mixins.addConfiguration(mixinFile);
                } else
                    log.error("Mixin file {} has invalid name!", mixinFile);
            }

            /* Initialize mod */
            try {
                modContainer.initialize();
                loadedMods.add(modContainer);
            } catch (Exception e) {
                log.error("Failed to initialize mod " + modInfo.name() + " (version: " + modInfo.version() + "): " + e.getMessage(), e);
                failures.incrementAndGet();
                return;
            }
        });
        if(failures.get() > 0) {
            log.error("Failed to initialize mods! Fix your server setup before trying again.");
            System.exit(1);
        }
        log.info("Done!");
    }
}
