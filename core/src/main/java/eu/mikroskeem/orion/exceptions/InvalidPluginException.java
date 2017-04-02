package eu.mikroskeem.orion.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Mark Vainomaa
 */
@RequiredArgsConstructor
@Getter
public class InvalidPluginException extends RuntimeException {
    private String message;
}
