package eu.mikroskeem.orion.api;

import eu.mikroskeem.shuriken.common.Ensure;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Mark Vainomaa
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Orion {
    @Getter private static OrionServer server = null;
    @Getter private static String version = null;

    public static void setServer(@NonNull OrionServer server){
        Ensure.ensureCondition(Orion.server == null,
                IllegalStateException.class,
                TypeWrapper.of("Orion server is already set!"));
        Orion.server = server;
    }

    public static void setVersion(@NonNull String version) {
        Ensure.ensureCondition(Orion.version == null,
                IllegalStateException.class,
                TypeWrapper.of("Orion version is already set!"));
        Orion.version = version;
    }
}
