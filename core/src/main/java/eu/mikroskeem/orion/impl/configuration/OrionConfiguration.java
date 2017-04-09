package eu.mikroskeem.orion.impl.configuration;

import eu.mikroskeem.orion.impl.configuration.categories.DebugCategory;
import eu.mikroskeem.orion.impl.configuration.categories.MessagesCategory;
import eu.mikroskeem.orion.impl.configuration.categories.SentryCategory;
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

    @Setting(value = "sentry", comment = "Sentry configuration")
    @Getter private SentryCategory sentry = new SentryCategory();
}
