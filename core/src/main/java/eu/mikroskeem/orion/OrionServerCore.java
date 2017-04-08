package eu.mikroskeem.orion;

import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.impl.configuration.StaticConfiguration;
import lombok.Getter;
import org.bukkit.Server;

/**
 * @author Mark Vainomaa
 */
public class OrionServerCore implements OrionServer {
    @Getter private static OrionServer instance;

    public OrionServerCore(Server server){
        instance = this;
    }

    @Override
    public Configuration getConfiguration() {
        return StaticConfiguration.INSTANCE;
    }
}
