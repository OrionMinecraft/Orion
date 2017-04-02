package eu.mikroskeem.orion.eventbus;

import eu.mikroskeem.orion.api.events.EventBus;
import eu.mikroskeem.orion.api.plugin.PluginContainer;

/**
 * @author Mark Vainomaa
 */
public interface EventBusFactory {
    EventBus getPluginEventBus(PluginContainer plugin);
}
