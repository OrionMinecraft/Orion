package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.entities.Player;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = CraftPlayer.class, remap = false)
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
        long currentTime = System.currentTimeMillis();
        return (currentTime - this.getLastActiveTime()) >= configuredAwayTime;
    }
}
