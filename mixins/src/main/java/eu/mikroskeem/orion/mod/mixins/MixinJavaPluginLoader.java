package eu.mikroskeem.orion.mod.mixins;

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
@Mixin(value = JavaPluginLoader.class, remap = false)
public class MixinJavaPluginLoader implements ExposedJavaPluginLoader {
    @Shadow @Final private List<PluginClassLoader> loaders;

    @Override
    public List<PluginClassLoader> getLoaders(){
        return this.loaders;
    }
}
