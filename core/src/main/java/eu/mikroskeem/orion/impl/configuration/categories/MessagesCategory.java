package eu.mikroskeem.orion.impl.configuration.categories;

import eu.mikroskeem.orion.api.server.Configuration;
import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * @author Mark Vainomaa
 */
@ConfigSerializable
public class MessagesCategory extends ConfigurationCategory {
    @Setting(value = "override-plugin-permission-denied-message",
            comment = "Should Orion override plugin's command permission denied message?")
    @Getter @Setter private boolean overridingPluginPermissionDeniedMessageEnabled = true;

    @Setting(value = "permission-denied-message",
            comment = "Command permission denied message. Defaults to Bukkit's default")
    @Getter @Setter private String commandPermissionDeniedMessage = Configuration.DEFAULT_COMMAND_PERMISSION_DENIED_MESSAGE;
}
