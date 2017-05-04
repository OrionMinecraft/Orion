package eu.mikroskeem.orion.api.server;

import java.io.IOException;

/**
 * @author Mark Vainomaa
 */
public interface Configuration {
    String DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE =
            "§cI'm sorry, but you do not have permission to perform this command. " +
                    "Please contact the server administrators if you believe that this is in error.";

    /**
     * Save configuration
     *
     * @throws IOException If configuration backend fails to save
     */
    default void save() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Load configuration
     *
     * @throws IOException If configuration backend fails to load
     */
    default void load() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Reload configuration
     *
     * @throws IOException If configuration backend fails to reload
     */
    default void reload() throws IOException {
        throw new UnsupportedOperationException();
    }

    /** Get messages subconfiguration */
    default Messages getMessages() {
        throw new UnsupportedOperationException();
    }

    /** Get debug subconfiguration */
    default Debug getDebug() {
        throw new UnsupportedOperationException();
    }

    /** Get commands subconfiguration */
    default Commands getCommands() {
        throw new UnsupportedOperationException();
    }

    /** Get sentry subconfiguration */
    default Sentry getSentry() {
        throw new UnsupportedOperationException();
    }

    /** Get player subconfiguration */
    default PlayerConfiguration getPlayerConfiguration() {
        throw new UnsupportedOperationException();
    }

    /** Debug subconfiguration */
    interface Debug {
        boolean isEventDumpingAllowed();
        boolean isScriptEventHandlerAllowed();
        boolean isReportingEventExceptionsToSentryAllowed();
        boolean isReportingCommandExceptionsToSentryAllowed();
        String getHastebinUrl();
    }

    /** Messages subconfiguration interface */
    interface Messages {
        String getCommandPermissionDeniedMessage();
    }

    /** Commands subconfiguration interface */
    interface Commands {
        boolean isOverridingPluginCommandPermissionDeniedMessageEnabled();
    }

    /** Sentry subconfiguration interface */
    interface Sentry {
        String getSentryDSN();
    }

    /** Player subconfiguration interface */
    interface PlayerConfiguration {
        long getMillisecondsUntilToMarkPlayerAway();
        boolean isPlayerDataSavingDisabled();
    }
}
