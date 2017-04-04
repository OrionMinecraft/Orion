package eu.mikroskeem.orion;

import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.eventbus.Event4jEventBusImpl;
import eu.mikroskeem.orion.eventbus.EventBusFactory;
import eu.mikroskeem.orion.impl.configuration.StaticConfiguration;
import lombok.Getter;
import org.bukkit.Server;

/**
 * @author Mark Vainomaa
 */
public class OrionServerCore implements OrionServer {
    @Getter private EventBusFactory eventBusFactory;
    @Getter private static OrionServer instance;

    public OrionServerCore(Server server){
        instance = this;
        eventBusFactory = new Event4jEventBusImpl();
    }

    @Override
    public Configuration getConfiguration() {
        return StaticConfiguration.INSTANCE;
    }
}
