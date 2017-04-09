package eu.mikroskeem.orion.impl.configuration.categories;

import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
public class SentryCategory extends ConfigurationCategory {
    @Setting(value = "sentry-dsn",
            comment = "Sentry Data Source Name, see See https://sentry.io")
    @Getter @Setter private String sentryDSN = "";
}
