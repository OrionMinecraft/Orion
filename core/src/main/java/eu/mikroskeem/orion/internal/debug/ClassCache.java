package eu.mikroskeem.orion.internal.debug;


import eu.mikroskeem.orion.internal.interfaces.ExposedJavaPluginLoader;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
public class ClassCache {
    private final static Map<String, Class<? extends Event>> availableEventClasses = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static void add(Class<?> clazz){
        availableEventClasses.put(clazz.getName(), (Class<? extends Event>)clazz);
    }

    public static Map<String, Class<? extends Event>> getEventClasses() {
        if(availableEventClasses.size() == 0) {
            synchronized (ClassCache.class) {
                FastClasspathScanner scanner = new FastClasspathScanner("")
                        .matchSubclassesOf(Event.class, ClassCache::add);
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if(plugin instanceof JavaPlugin && plugin.getPluginLoader() instanceof JavaPluginLoader) {
                        ((ExposedJavaPluginLoader) plugin.getPluginLoader()).getLoaders()
                                .forEach(scanner::addClassLoader);
                    }
                }
                scanner.scan();
            }
        }
        return availableEventClasses;
    }
}
