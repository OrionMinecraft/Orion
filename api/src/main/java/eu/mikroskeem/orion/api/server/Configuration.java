package eu.mikroskeem.orion.api.server;

import java.io.IOException;

/**
 * @author Mark Vainomaa
 */
public interface Configuration {
    String DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE =
            "§cI'm sorry, but you do not have permission to perform this command. " +
                    "Please contact the server administrators if you believe that this is in error.";

    default void save() throws IOException {
        throw new UnsupportedOperationException();
    }

    default void load() throws IOException {
        throw new UnsupportedOperationException();
    }

    default void reload() throws IOException {
        throw new UnsupportedOperationException();
    }

    default Messages getMessages() {
        throw new UnsupportedOperationException();
    }

    default Debug getDebug() {
        throw new UnsupportedOperationException();
    }

    default Commands getCommands() {
        throw new UnsupportedOperationException();
    }

    default Sentry getSentry() {
        throw new UnsupportedOperationException();
    }

    default PlayerConfiguration getPlayerConfiguration() {
        throw new UnsupportedOperationException();
    }

    /* Debug subconfiguration */
    interface Debug {
        boolean isEventDumpingAllowed();
        boolean isScriptEventHandlerAllowed();
        boolean isReportingEventExceptionsToSentryAllowed();
        boolean isReportingCommandExceptionsToSentryAllowed();
        String getHastebinUrl();
    }

    /* Messages subconfiguration */
    interface Messages {
        String getCommandPermissionDeniedMessage();
    }

    /* Commands subconfiguration */
    interface Commands {
        boolean isOverridingPluginCommandPermissionDeniedMessageEnabled();
    }

    /* Sentry subconfiguration */
    interface Sentry {
        String getSentryDSN();
    }

    /* Player subconfiguration */
    interface PlayerConfiguration {
        long getMillisecondsUntilToMarkPlayerAway();
    }
}
