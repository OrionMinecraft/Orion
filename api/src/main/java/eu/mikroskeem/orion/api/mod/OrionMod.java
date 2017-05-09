package eu.mikroskeem.orion.api.mod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark Orion mods
 *
 * @author Mark Vainomaa
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OrionMod {
    /** Mod name */
    String name();

    /** Mod version. Must use <a href="http://semver.org/">Semantic Versioning</a> format */
    String version();

    /** Mod author(s) */
    String[] authors() default {};

    /**
     * Mixin configuration names. <br/>
     * Leave empty to not configure mixins (though that is the key feature of
     * Orion mods currently).
     * Mixin configurations must use <pre>mixins.PLUGINNAME.MIXINNAME.json</pre> scheme, otherwise
     * they won't be loaded.
     *
     * @return Mixin configuration names
     */
    String[] mixins() default {};
}
