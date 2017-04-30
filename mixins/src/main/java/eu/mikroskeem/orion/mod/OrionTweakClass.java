package eu.mikroskeem.orion.mod;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Orion Tweak class to load Mixins for CraftBukkit-derivatives
 *
 * @author Mark Vainomaa
 */
@Slf4j
public class OrionTweakClass implements ITweaker {
    public static String launchTarget;
    @Getter private String[] launchArguments;

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
    }

    private void setupMixins(){
        log.info("Setting up mixins...");
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
}
