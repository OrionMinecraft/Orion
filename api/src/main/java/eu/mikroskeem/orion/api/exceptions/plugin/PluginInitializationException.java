package eu.mikroskeem.orion.api.exceptions.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
@Getter
public class PluginInitializationException extends RuntimeException {
    private String message;
}
