package eu.mikroskeem.orion.internal.debug;

import eu.mikroskeem.orion.api.events.Event;
import groovy.lang.Script;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
public class DebugListener<T extends Event> implements Listener {
    @Getter private final Class<T> eventClass;
    @Getter private final String listenerName;
    private final Script compiledScript;

    public Object execute(T event){
        compiledScript.setProperty("event", event);
        return compiledScript.run();
    }
}
