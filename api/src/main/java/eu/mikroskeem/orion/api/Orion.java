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

    public static void setServer(@NonNull OrionServer server){
        Ensure.ensureCondition(Orion.server == null,
                IllegalStateException.class,
                TypeWrapper.of("Orion server is already set!"));
        Orion.server = server;
    }
}
