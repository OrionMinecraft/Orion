package eu.mikroskeem.orion.internal.debug;

import eu.mikroskeem.orion.api.events.Event;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
public class ClassCache {
    private final static List<Class<? extends Event>> availableEventClasses = new ArrayList<>();

    public static List<Class<? extends Event>> getEventClasses() {
        if(availableEventClasses.size() == 0) {
            synchronized (ClassCache.class) {
                List<Class<? extends Event>> found = new ArrayList<>();
                new FastClasspathScanner("").matchClassesImplementing(Event.class, found::add).scan();
                availableEventClasses.addAll(found);
            }
        }
        return availableEventClasses;
    }
}
