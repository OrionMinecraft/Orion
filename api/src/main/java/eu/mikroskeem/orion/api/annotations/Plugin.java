package eu.mikroskeem.orion.api.annotations;

/**
 * @author Mark Vainomaa
 */
public @interface Plugin {
    String name();
    String displayName() default "";
    String version();
    String[] dependencies() default {};
    String[] softDependencies() default {};
}
