package eu.mikroskeem.orion.launcher;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.launchwrapper.Launch;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Orion Launcher wrapper for server, based on Minecraft LegacyLauncher
 *
 * @author Mark Vainomaa
 * @version 0.0.1
 */
@Slf4j
public class Bootstrap {
    public static void main(String... args) {
        /* Set up SLF4J configuration */
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(Bootstrap.class.getResourceAsStream("/orion_logback.xml"));
        } catch (JoranException e){
            e.printStackTrace();
        }

        log.info("Orion Launcher by mikroskeem");
        List<String> finalArgs = new ArrayList<>(Arrays.asList(
                "--tweakClass",
                OrionTweakClass.class.getName()
        ));
        finalArgs.addAll(Arrays.asList(args));

        log.info("Starting LegacyLauncher with arguments: {}", finalArgs);
        Launch.main(finalArgs.toArray(new String[0]));
    }
}
