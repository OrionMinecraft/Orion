package eu.mikroskeem.orion.internal.debug;

import com.google.common.collect.ImmutableList;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;
import org.bukkit.event.Event;
import org.codehaus.groovy.control.CompilationFailedException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manage debug listeners created with commands
 *
 * @author Mark Vainomaa
 */
public class DebugListenerManager {
    @Getter private static final Map<String, DebugListener<?>> listeners = new HashMap<>();

    public static void register(String listenerName, Class<? extends Event> eventClass, String rawScript)
            throws CompilationFailedException  {
        Script script = new GroovyShell().parse(rawScript);
        DebugListener<?> listener = listeners.computeIfAbsent(listenerName, l -> {
            return new DebugListener<>(eventClass, listenerName, script);
        });
    }

    public static void unregister(String listenerName) {
        listeners.computeIfPresent(listenerName, (l,k) -> null);
    }

    public static boolean listenerExists(String listenerName) {
        return listeners.containsKey(listenerName);
    }

    @SuppressWarnings("unchecked")
    public static Collection<DebugListener<?>> getListenersForEvent(Class<? extends Event> eventClass){
        return ImmutableList.copyOf(listeners.values().stream()
                .filter(debugListener -> debugListener.getEventClass() == eventClass)
                .collect(Collectors.toList()));
    }
}
