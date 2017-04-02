package eu.mikroskeem.orion.inject;

import com.google.inject.AbstractModule;
import eu.mikroskeem.orion.api.plugin.PluginManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.messaging.Messenger;

/**
 * @author Mark Vainomaa
 */
public class BukkitModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Server.class).toInstance(Bukkit.getServer());
        bind(PluginManager.class).toInstance((PluginManager)Bukkit.getServer().getPluginManager());
        bind(Messenger.class).toInstance(Bukkit.getMessenger());
    }
}
