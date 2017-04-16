package eu.mikroskeem.orion.impl.configuration;

import eu.mikroskeem.orion.impl.configuration.categories.*;
import lombok.Getter;
import ninja.leaping.configurate.objectmapping.Setting;

/**
 * @author Mark Vainomaa
 */
public class OrionConfiguration {
    @Setting(value = "debug", comment = "Server debugging")
    @Getter private DebugCategory debug = new DebugCategory();

    @Setting(value = "messages", comment = "Messages")
    @Getter private MessagesCategory messages = new MessagesCategory();

    @Setting(value = "commands", comment = "Commands system configuration")
    @Getter private CommandsCategory commands = new CommandsCategory();

    @Setting(value = "sentry", comment = "Sentry configuration")
    @Getter private SentryCategory sentry = new SentryCategory();

    @Setting(value = "player", comment = "Player configuration")
    @Getter private PlayerConfigurationCategory playerConfiguration = new PlayerConfigurationCategory();
}
