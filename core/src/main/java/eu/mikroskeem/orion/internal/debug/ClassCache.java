package eu.mikroskeem.orion.internal.debug;

import eu.mikroskeem.orion.api.events.Event;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
public class ClassCache {
    private final static List<Class<? extends Event>> availableEventClasses = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private static void add(Class<?> clazz){
        /* Note: Mixin makes Bukkit event classes compatible with my classes */
        availableEventClasses.add((Class<? extends Event>)clazz);
    }

    public static List<Class<? extends Event>> getEventClasses() {
        if(availableEventClasses.size() == 0) {
            synchronized (ClassCache.class) {
                new FastClasspathScanner("")
                        .addClassLoader(Bukkit.class.getClassLoader())
                        .matchClassesImplementing(Event.class, ClassCache::add)
                        .matchSubclassesOf(org.bukkit.event.Event.class, ClassCache::add)
                        .scan();
            }
        }
        return availableEventClasses;
    }
}
