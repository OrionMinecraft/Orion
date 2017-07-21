package eu.mikroskeem.orion.core.lcl;

import eu.mikroskeem.shuriken.instrumentation.methodreflector.MethodReflector;
import eu.mikroskeem.shuriken.instrumentation.methodreflector.TargetFieldGetter;
import eu.mikroskeem.shuriken.reflect.Reflect;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.util.Set;
import java.util.WeakHashMap;


/**
 * @author Mark Vainomaa
 */
public interface LaunchClassLoaderAccessor {
    @TargetFieldGetter("classLoaderExceptions") Set<String> getClassLoaderExceptions();

    class WrappedInstances {
        private final static WeakHashMap<LaunchClassLoader, LaunchClassLoaderAccessor> instances = new WeakHashMap<>();

        public static LaunchClassLoaderAccessor getAccessor(LaunchClassLoader launchClassLoader) {
            return instances.computeIfAbsent(launchClassLoader, lcl ->
                MethodReflector.newInstance(Reflect.wrapInstance(lcl), LaunchClassLoaderAccessor.class).getReflector()
            );
        }
    }
}
