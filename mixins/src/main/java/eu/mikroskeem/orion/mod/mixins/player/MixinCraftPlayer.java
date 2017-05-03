package eu.mikroskeem.orion.mod.mixins.player;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.entities.Player;
import eu.mikroskeem.orion.api.events.player.chat.PlayerPluginSendMessageEvent;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.*;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = CraftPlayer.class, remap = false)
@Implements(@Interface(iface = CommandSender.class, prefix = "orion$"))
public abstract class MixinCraftPlayer implements Player {
    @Shadow public abstract EntityPlayer getHandle();

    @Override
    public long getLastActiveTime() {
        return this.getHandle().I();
    }

    @Override
    public boolean isAway() {
        long configuredAwayTime = Orion.getServer().getConfiguration()
                .getPlayerConfiguration()
                .getMillisecondsUntilToMarkPlayerAway();
        long currentTime = MinecraftServer.aw();
        return (currentTime - this.getLastActiveTime()) >= configuredAwayTime;
    }

    @Intrinsic(displace = true)
    public void orion$sendMessage(String message) {
        PlayerPluginSendMessageEvent event = new PlayerPluginSendMessageEvent(this, message);
        getServer().getPluginManager().callEvent(event);
        if(!event.isCancelled()) {
            sendMessage(message);
        }
    }
}
