package eu.mikroskeem.orion.eventbus;

import eu.mikroskeem.orion.api.events.Event;
import eu.mikroskeem.orion.api.plugin.PluginContainer;
import lombok.RequiredArgsConstructor;
import net.techcable.event4j.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author Mark Vainomaa
 */
public class Event4jEventBusImpl implements EventBusFactory {
    private Map<Class<? extends Event>, ListenerWrapper> listeners = new HashMap<>();
    private EventBus<Event, Object> eventBus = EventBus.builder()
            .eventClass(Event.class)
            .build();

    @Override
    public eu.mikroskeem.orion.api.events.EventBus getPluginEventBus(PluginContainer plugin) {
        return new PluginEventBus(plugin);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private class ListenerWrapper<T extends Event> {
        private final PluginContainer pluginContainer;
        private final Class<T> eventClass;
        private final Consumer<T> listener;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public class PluginEventBus implements eu.mikroskeem.orion.api.events.EventBus {
        final PluginContainer plugin;

        @Override
        public <T extends Event> T fire(T event) {
            Event4jEventBusImpl.this.eventBus.fire(event);
            return event;
        }

        @Override
        public <T extends Event> void register(Class<T> eventClass, Consumer<T> listener) {
            ListenerWrapper<T> wrapper = new ListenerWrapper<>(plugin, eventClass, listener);
            listeners.put(eventClass, wrapper);
            Event4jEventBusImpl.this.eventBus.register(wrapper);
        }

        @Override
        public <T extends Event> void unregister(Class<T> eventClass, Consumer<T> listener) {
            new HashMap<>(listeners).forEach((clazz, wrapper) -> {
                if(plugin.equals(wrapper.pluginContainer) &&
                   eventClass.equals(wrapper.eventClass) &&
                   listener.equals(wrapper.listener)
                ){
                    listeners.remove(eventClass, wrapper);
                }
            });
        }

        @Override
        public <T extends Event> void unregisterAll(Class<T> eventClass) {
            new HashMap<>(listeners).forEach((clazz, wrapper) -> {
                if(plugin.equals(wrapper.pluginContainer)){
                    listeners.remove(eventClass, wrapper);
                }
            });
        }
    }
}
