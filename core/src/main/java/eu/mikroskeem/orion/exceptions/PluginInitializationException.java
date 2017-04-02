package eu.mikroskeem.orion.exceptions;

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
