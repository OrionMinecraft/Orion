package eu.mikroskeem.orion;

import eu.mikroskeem.orion.api.events.EventBus;
import eu.mikroskeem.orion.eventbus.Event4jEventBusImpl;
import eu.mikroskeem.orion.eventbus.EventBusFactory;
import lombok.Getter;

/**
 * @author Mark Vainomaa
 */
public class OrionCore {
    @Getter private EventBusFactory eventBusFactory;

    public OrionCore(){
        eventBusFactory = new Event4jEventBusImpl();
    }
}
