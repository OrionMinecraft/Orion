package eu.mikroskeem.orion.impl.configuration.categories;

import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
public class CommandsCategory extends ConfigurationCategory {
    @Setting(value = "override-plugin-command-permission-denied-message",
            comment = "Should Orion override plugin's command permission denied message?")
    @Getter @Setter private boolean overridingPluginCommandPermissionDeniedMessageEnabled = true;
}
