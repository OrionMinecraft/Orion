package eu.mikroskeem.orion.core.launcher;

import eu.mikroskeem.orion.api.bytecode.OrionTransformer;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * @author Mark Vainomaa
 */
public interface LauncherService {
    @NotNull
    Map<String, Object> getBlackBoard();

    void registerTransformer(@NotNull Class<? extends OrionTransformer> transformer);

    @NotNull
    Set<String> getClassLoaderExclusions();

    @NotNull
    ClassLoader getClassLoader();

    void addURLToClassLoader(@NotNull URL url);
}
