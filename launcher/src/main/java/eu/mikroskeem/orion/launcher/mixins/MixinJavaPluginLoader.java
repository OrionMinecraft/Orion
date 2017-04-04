package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.internal.interfaces.ExposedJavaPluginLoader;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * @author Mark Vainomaa
 */
@Mixin(JavaPluginLoader.class)
public class MixinJavaPluginLoader implements ExposedJavaPluginLoader {
    @Shadow(remap = false) @Final private List<PluginClassLoader> loaders;

    @Override
    public List<PluginClassLoader> getLoaders(){
        return this.loaders;
    }
}
