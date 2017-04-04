package eu.mikroskeem.orion.api.plugin.stub;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;

import java.io.File;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

public abstract class AbstractFakePlugin extends PluginBase implements PluginLoader {
    public final Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public final PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Pattern[] getPluginFileFilters() {
        return new Pattern[0];
    }

    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void enablePlugin(Plugin plugin) {}
    public void disablePlugin(Plugin plugin) {}

    @Override public boolean isEnabled() { return true; }
    public PluginLoader getPluginLoader()
    {
        return this;
    }

    private interface Excludes {
        PluginLoader getPluginLoader();
        PluginDescriptionFile getDescription();
        String getName();
        boolean isEnabled();
    }

    /* Forward most calls to parent plugin */
    @Delegate(excludes = AbstractFakePlugin.Excludes.class, types = {
            CommandExecutor.class, TabCompleter.class, Plugin.class
    })
    private final Plugin plugin;

    @Getter private PluginDescriptionFile description;
    public AbstractFakePlugin(Plugin plugin,
                              String pluginName,
                              String pluginVersion,
                              String mainClass,
                              String descriptionStr,
                              String author){
        this.plugin = plugin;

        /* Generate plugin description file */
        StringJoiner sj = new StringJoiner("\n");
        sj.add(String.format("name: %s", pluginName));
        sj.add(String.format("version: %s", pluginVersion==null?"1.0-STUB":pluginVersion));
        sj.add(String.format("description: %s", descriptionStr));
        sj.add(String.format("author: %s", author));
        sj.add(String.format("main: %s", mainClass));

        try {
            description = new PluginDescriptionFile(new StringReader(sj.toString()));
        } catch (InvalidDescriptionException e){
            e.printStackTrace();
        }
    }

    @Override public String toString() {
        return description.getFullName();
    }
}