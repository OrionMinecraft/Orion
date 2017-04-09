package eu.mikroskeem.orion.impl.configuration.categories;

import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
public class DebugCategory extends ConfigurationCategory {
    @Setting(value = "event-dumping-allowed",
            comment = "Whether to allow debug logging of CraftBukkit events or not")
    @Getter @Setter private boolean eventDumpingAllowed = false;

    @Setting(value = "script-eventhandlers",
            comment = "Whether to allow adding event handlers written using Groovy or not")
    @Getter @Setter private boolean scriptEventHandlerAllowed = false;

    @Setting(value = "report-event-exceptions-to-sentryio",
            comment = "Whether to report event exceptions to https://sentry.io or not")
    @Getter @Setter private boolean reportingEventExceptionsToSentryAllowed = false;

    @Setting(value = "report-command-exceptions-to-sentryio",
            comment = "Whether to report command exceptions to https://sentry.io or not")
    @Getter @Setter private boolean reportingCommandExceptionsToSentryAllowed = false;
}
