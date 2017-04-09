package eu.mikroskeem.orion;

import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.impl.configuration.HoconFileConfiguration;
import lombok.Getter;

import java.nio.file.Paths;

/**
 * @author Mark Vainomaa
 */
public class OrionServerCore implements OrionServer {
    @Getter private static OrionServer instance;
    @Getter private Configuration configuration;

    public OrionServerCore(){
        instance = this;
        configuration = new HoconFileConfiguration(Paths.get(".", "orion.cfg"));
    }
}
