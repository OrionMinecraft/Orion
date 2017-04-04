package eu.mikroskeem.orion.launcher.mixins;

import eu.mikroskeem.orion.internal.interfaces.ExposedPluginClassLoader;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import org.bukkit.plugin.java.PluginClassLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

/**
 * @author Mark Vainomaa
 */
@Mixin(PluginClassLoader.class)
public abstract class MixinPluginClassLoader extends URLClassLoader implements ExposedPluginClassLoader {
    public MixinPluginClassLoader(URL[] urls) {super(urls);}

    @Shadow(remap = false) @Final private Map<String, Class<?>> classes;
    private final Map<String, Package> orion$generatedPackages = new HashMap<>();

    @Override
    protected Package getPackage(String name) {
        /* Uhh, let's work around LaunchWrapper quirk */
        switch (name){
            case "me.konsolas.aac":
                return orion$generatedPackages.computeIfAbsent(name, key -> {
                    try {
                        URL jarUrl = getURLs()[0];
                        File pluginFile = new File(jarUrl.getFile());
                        try(
                                ZipFile zf = new ZipFile(pluginFile);
                                InputStream mfIs = zf.getInputStream(zf.getEntry("META-INF/MANIFEST.MF"))
                        ) {
                            Manifest manifest = new Manifest(mfIs);
                            Package thePackage = Reflect
                                    .wrapClass(Package.class)
                                    .construct(
                                            TypeWrapper.of(name),
                                            TypeWrapper.of(manifest),
                                            TypeWrapper.of(jarUrl),
                                            TypeWrapper.of(ClassLoader.class, this)
                                    )
                                    .getClassInstance();
                            return thePackage;
                        }
                    } catch (Exception e){
                        SneakyThrow.throwException(e);
                    }
                    return null;
                });
            default:
                return super.getPackage(name);
        }
    }

    @Override
    public Map<String, Class<?>> getClassesMap() {
        return this.classes;
    }
}
