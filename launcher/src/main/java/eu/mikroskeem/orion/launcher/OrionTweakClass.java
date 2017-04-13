package eu.mikroskeem.orion.launcher;

import eu.mikroskeem.orion.launcher.transformers.AccessLevelTransformer;
import eu.mikroskeem.orion.launcher.transformers.SuperTransformer;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarInputStream;

/**
 * Orion Tweak class to load Mixins for CraftBukkit-derivatives
 *
 * @author Mark Vainomaa
 */
@Slf4j
public class OrionTweakClass implements ITweaker {
    private URL paperclipUrl;
    @Getter private String[] launchArguments;
    @Getter private String launchTarget;

    @SneakyThrows
    public OrionTweakClass(){
        File paperclipJar = new File("cache/patched_1.11.2.jar");

        try(FileInputStream fs = new FileInputStream(paperclipJar); JarInputStream js = new JarInputStream(fs)){
            launchTarget = js.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException e) {
            throw new RuntimeException("Error opening " + paperclipJar, e);
        }
        paperclipUrl = paperclipJar.toURI().toURL();
    }

    @Override
    public void acceptOptions(List<String> args) {
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
    public void injectIntoClassLoader(LaunchClassLoader launchClassLoader) {
        /* Load paperclip jar */
        launchClassLoader.addURL(paperclipUrl);

        /* Load dependencies */
        Bootstrap.serverDependenciesList.forEach(launchClassLoader::addURL);

        /* Classloader exclusions */
        String[] loadExclusions = new String[] {
                "com.mojang.util.QueueLogAppender",     /* Mojang's Log4j2 plugin */
        };
        for (String exclusion : loadExclusions) launchClassLoader.addClassLoaderExclusion(exclusion);

        /* Transformer exclusions */
        String[] transformExclusions = new String[] {
                "eu.mikroskeem.orion.",                 /* Orion */
                "co.aikar.taskchain.",                  /* TaskChain */
                "org.aopalliance.",                     /* aopallicance */
                "ch.qos.logback.",                      /* Logback */
                "org.slf4j.",                           /* SLF4J */
                "com.github.zafarkhaja.semver.",        /* Semver */
                "com.google.",                          /* Google libraries */
                "com.googlecode.",
                "com.squareup.",                        /* OkHttp + dependencies */
                "com.typesafe.",                        /* Typesafe configuration */
                "ninja.leaping.",                       /* Configurate */
                "org.jetbrains.annotations.",            /* JetBrains annotations */
                "javax."
        };
        for (String exclusion : transformExclusions) launchClassLoader.addTransformerExclusion(exclusion);

        /* Set up mixins and transformers */
        setupMixins();
        setupTransformers(launchClassLoader);
    }

    private void setupMixins(){
        log.info("Setting up mixins...");
        /* Set up mixin framework */
        MixinBootstrap.init();
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.SERVER);

        /* Mixins */
        Mixins.addConfiguration("orion.mixins.json");

        /* Ready */
        log.info("Done!");
    }

    private void setupTransformers(LaunchClassLoader launchClassLoader) {
        log.info("Setting up transformers...");
        List<String> transformers = Arrays.asList(
                AccessLevelTransformer.class.getName(),
                SuperTransformer.class.getName()
        );
        transformers.forEach(launchClassLoader::registerTransformer);

        /* Ready */
        log.info("Done!");
    }
}
