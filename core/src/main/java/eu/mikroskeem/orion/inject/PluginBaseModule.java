package eu.mikroskeem.orion.inject;

import com.google.inject.AbstractModule;
import eu.mikroskeem.orion.OrionCore;
import eu.mikroskeem.orion.api.events.EventBus;
import eu.mikroskeem.orion.api.plugin.PluginContainer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
public class PluginBaseModule extends AbstractModule {
    private final PluginContainer pluginContainer;
    private OrionCore core;

    @Override
    protected void configure() {
        bind(Logger.class).toProvider(pluginContainer::getLogger);
        bind(EventBus.class).toProvider(() -> core.getEventBusFactory().getPluginEventBus(pluginContainer));
    }
}
