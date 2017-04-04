package eu.mikroskeem.orion.impl.configuration;

import eu.mikroskeem.orion.api.server.Configuration;

/**
 * @author Mark Vainomaa
 */
public class StaticConfiguration implements Configuration {
    public static final StaticConfiguration INSTANCE = new StaticConfiguration();

    private final static String DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE =
            "§cI'm sorry, but you do not have permission to perform this command." +
            "Please contact the server administrators if you believe that this is in error.";
    public static String COMMAND_PERMISSION_DENIED_MESSAGE = DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE;

    @Override
    public String getCommandPermissionDeniedMessage() {
        return COMMAND_PERMISSION_DENIED_MESSAGE;
    }

    @Override
    public void setCommandPermissionDeniedMessage(String message) {
        COMMAND_PERMISSION_DENIED_MESSAGE = message != null? message: DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE;
    }
}
