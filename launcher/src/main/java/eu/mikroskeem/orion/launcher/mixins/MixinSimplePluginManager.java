package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.api.plugin.PluginManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

/**
 * @author Mark Vainomaa
 */
@Mixin(SimplePluginManager.class)
public abstract class MixinSimplePluginManager implements PluginManager {
    @Shadow(remap = false) @Final private Map<String, Plugin> lookupNames;
    @Shadow(remap = false) @Final private List<Plugin> plugins;

    public Map<String, Plugin> getLookupNames(){
        return this.lookupNames;
    }

    public List<Plugin> listPlugins(){
        return this.plugins;
    }
}
