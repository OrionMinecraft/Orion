package eu.mikroskeem.orion.api.server;

/**
 * @author Mark Vainomaa
 */
public interface Configuration {
    String getCommandPermissionDeniedMessage();
    void setCommandPermissionDeniedMessage(String message);
}
