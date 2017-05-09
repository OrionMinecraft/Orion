package eu.mikroskeem.orion.api.mod;

import com.github.zafarkhaja.semver.Version;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Orion Mod container
 *
 * @author Mark Vainomaa
 */
public class OrionModContainer {
    private final Class<?> modClass;
    private final ModInfo modInfo;
    private Object modInstance;

    public OrionModContainer(@NotNull Class<?> modClass, @NotNull ModInfo modInfo) {
        this.modClass = modClass;
        this.modInfo = modInfo;
    }

    @SneakyThrows
    public void initialize() {
        if(modInstance == null) {
            modInstance = modClass.newInstance();
        } else {
            throw new IllegalStateException("Mod is already initialized!");
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class ModInfo {
        /** Mod version */
        private final Version version;

        /** Mod authors */
        private final List<String> authors;
    }
}
