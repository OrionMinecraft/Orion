package eu.mikroskeem.orion.inject;

import com.google.inject.AbstractModule;
import eu.mikroskeem.orion.OrionServerCore;
import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.plugin.PluginContainer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.impl.configuration.StaticConfiguration;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
public class PluginBaseModule extends AbstractModule {
    private final PluginContainer pluginContainer;
    private OrionServerCore core;

    @Override
    protected void configure() {
        bind(OrionServer.class).toInstance(core);
        bind(Logger.class).toProvider(pluginContainer::getLogger);
        //bind(EventBus.class).toProvider(() -> core.getEventBusFactory().getPluginEventBus(pluginContainer));
        bind(Configuration.class).toInstance(StaticConfiguration.INSTANCE);
    }
}
