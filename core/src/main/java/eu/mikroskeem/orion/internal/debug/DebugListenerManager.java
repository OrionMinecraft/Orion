package eu.mikroskeem.orion.internal.debug;

import com.google.common.collect.ImmutableList;
import eu.mikroskeem.orion.api.events.Event;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
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
    private static final Map<String, DebugListener<?>> listeners = new HashMap<>();

    public void register(String listenerName, Class<? extends Event> eventClass, String rawScript)
            throws CompilationFailedException  {
        Script script = new GroovyShell().parse(rawScript);
        DebugListener<?> listener = listeners.computeIfAbsent(listenerName, l -> {
            return new DebugListener<>(eventClass, listenerName, script);
        });
    }

    public void unregister(String listenerName) {
        listeners.computeIfPresent(listenerName, (l,k) -> null);
    }

    public boolean listenerExists(String listenerName) {
        return listeners.containsKey(listenerName);
    }

    @SuppressWarnings("unchecked")
    public static Collection<DebugListener<?>> getListenersForEvent(Class<? extends Event> eventClass){
        return ImmutableList.copyOf(listeners.values().stream()
                .filter(debugListener -> debugListener.getEventClass() == eventClass)
                .collect(Collectors.toList()));
    }
}
