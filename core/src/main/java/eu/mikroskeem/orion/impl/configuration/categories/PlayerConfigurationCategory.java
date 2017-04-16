package eu.mikroskeem.orion.impl.configuration.categories;

import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
public class PlayerConfigurationCategory extends ConfigurationCategory {
    @Setting(value = "milliseconds-until-to-mark-player-away",
            comment = "Time how long should player be idle to mark one away")
    @Getter @Setter private long millisecondsUntilToMarkPlayerAway = 120 * 1000;
}
