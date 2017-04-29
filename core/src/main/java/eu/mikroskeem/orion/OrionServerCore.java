package eu.mikroskeem.orion;

import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.api.world.World;
import eu.mikroskeem.orion.impl.configuration.HoconFileConfiguration;
import eu.mikroskeem.orion.internal.debug.sentry.SentryReporter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mark Vainomaa
 */
@Slf4j
public class OrionServerCore implements OrionServer {
    @Getter private static OrionServer instance;
    @Getter private Configuration configuration;
    @Getter private final SentryReporter sentryReporter;

    public OrionServerCore(){
        instance = this;
        configuration = new HoconFileConfiguration(Paths.get(".", "orion.cfg"));
        try {
            configuration.load();
            configuration.save();
        } catch (IOException e){
            log.error("Failed to load Orion configuration!", e);
        }

        sentryReporter = new SentryReporter(this);
    }

    @Override
    public World getWorld(String worldName) {
        return (World)Bukkit.getWorld(worldName);
    }

    @Override
    public List<World> getWorlds() {
        return Bukkit.getWorlds().stream().map(w -> (World)w).collect(Collectors.toList());
    }
}
