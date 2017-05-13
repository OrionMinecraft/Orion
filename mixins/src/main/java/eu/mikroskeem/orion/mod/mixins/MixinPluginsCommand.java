package eu.mikroskeem.orion.mod.mixins;

import com.google.common.collect.ImmutableList;
import eu.mikroskeem.orion.internal.interfaces.ExposedPluginManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.PluginsCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = PluginsCommand.class, remap = false)
public abstract class MixinPluginsCommand extends Command {
    public MixinPluginsCommand(){super(null, null, null, ImmutableList.of()); }

    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;
        sender.sendMessage(getFancyPluginList().toArray(new BaseComponent[0]));
        return true;
    }

    private List<BaseComponent> getFancyPluginList() {
        ExposedPluginManager plm = (ExposedPluginManager)Bukkit.getPluginManager();
        TextComponent separator = new TextComponent(", ");
        separator.setColor(ChatColor.GRAY);
        List<BaseComponent> pluginComponents = new ArrayList<>();

        TextComponent pluginsText = new TextComponent(String.format("Plugins (%s): ", plm.listPlugins().size()));
        pluginComponents.add(pluginsText);

        plm.listPlugins().forEach(plugin -> {
            TextComponent pluginComponent = new TextComponent();
            pluginComponent.setText(plugin.getName());
            pluginComponent.setColor(orion$isEnabled(plugin));
            pluginComponent.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    orion$generatePluginHover(plugin).toArray(new BaseComponent[0])
            ));
            pluginComponent.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/bukkit:version " + plugin.getName()
            ));
            pluginComponents.add(pluginComponent);
            pluginComponents.add(separator);
        });
        pluginComponents.remove(pluginComponents.size()-1);

        return pluginComponents;
    }

    private ChatColor orion$isEnabled(Plugin plugin){
        return plugin.isEnabled()? ChatColor.GREEN:ChatColor.RED;
    }

    /* Plugin info generation */
    private List<BaseComponent> orion$generatePluginHover(Plugin plugin) {
        List<BaseComponent> hoverComponents = new ArrayList<>();
        BaseComponent newLine = new TextComponent("\n");
        PluginDescriptionFile description = plugin.getDescription();
        hoverComponents.add(orion$generatePluginName(plugin));
        hoverComponents.add(newLine);
        hoverComponents.add(orion$generatePluginVersion(plugin));
        hoverComponents.add(newLine);
        hoverComponents.add(orion$generateAuthors(description.getAuthors()));
        hoverComponents.add(newLine);
        hoverComponents.add(orion$generateDescription(description.getDescription()));
        return hoverComponents;
    }

    private BaseComponent orion$generatePluginName(Plugin plugin) {
        TextComponent nameBase = new TextComponent("Plugin name: ");
        TextComponent nameItself = new TextComponent(plugin.getName());
        nameBase.setColor(ChatColor.GRAY);
        nameItself.setColor(orion$isEnabled(plugin));
        nameBase.addExtra(nameItself);
        return nameBase;
    }

    private BaseComponent orion$generatePluginVersion(Plugin plugin) {
        TextComponent versionBase = new TextComponent("Version: ");
        TextComponent versionItself = new TextComponent(plugin.getDescription().getVersion());
        versionBase.setColor(ChatColor.GRAY);
        versionItself.setColor(ChatColor.GREEN);
        versionBase.addExtra(versionItself);
        return versionBase;
    }

    private BaseComponent orion$generateAuthors(List<String> authors) {
        TextComponent authorsBase = new TextComponent("Authors: ");
        authorsBase.setColor(ChatColor.GRAY);
        if(authors.size() == 0) {
            TextComponent noneText = new TextComponent("Unspecified");
            noneText.setColor(ChatColor.RED);
            authorsBase.addExtra(noneText);
        } else if(authors.size() == 1) {
            authorsBase.setText("Author: ");
            TextComponent oneAuthor = new TextComponent(authors.get(0));
            oneAuthor.setColor(ChatColor.GREEN);
            authorsBase.addExtra(oneAuthor);
        } else {
            authors.forEach(strAuthor -> {
                TextComponent author = new TextComponent(strAuthor);
                author.setColor(ChatColor.GREEN);
                authorsBase.addExtra(author);

                TextComponent separator = new TextComponent(", ");
                separator.setColor(ChatColor.GRAY);
                authorsBase.addExtra(separator);
            });
            authorsBase.getExtra().remove(authorsBase.getExtra().size()-1);
        }
        return authorsBase;
    }

    private BaseComponent orion$generateDescription(String description) {
        TextComponent descriptionBase = new TextComponent("Description: ");
        descriptionBase.setColor(ChatColor.GRAY);
        if(description != null) {
            TextComponent descriptionLine = new TextComponent();
            for (BaseComponent baseComponent : TextComponent.fromLegacyText(description))
                descriptionBase.addExtra(baseComponent);
            descriptionBase.addExtra(descriptionLine);
        } else {
            TextComponent noneText = new TextComponent("Unspecified");
            noneText.setColor(ChatColor.RED);
            descriptionBase.addExtra(noneText);
        }
        return descriptionBase;
    }
}
