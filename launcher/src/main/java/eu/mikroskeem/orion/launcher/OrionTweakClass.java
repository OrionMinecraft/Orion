package eu.mikroskeem.orion.launcher;

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
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        if(gameDir == null) gameDir = new File("");
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

        /* Exclude log4j2 */
        launchClassLoader.addClassLoaderExclusion("org.apache.logging.log4j.*");

        /* Fix ze logging */
        launchClassLoader.addClassLoaderExclusion("net.minecraftforge.server.console.TerminalConsoleAppender");
        launchClassLoader.addClassLoaderExclusion("com.mojang.util.QueueLogAppender");

        /* Set up mixins */
        setupMixins();
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
}
