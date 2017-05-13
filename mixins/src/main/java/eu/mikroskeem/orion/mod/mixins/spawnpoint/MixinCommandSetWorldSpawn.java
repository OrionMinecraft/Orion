package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import net.minecraft.server.v1_11_R1.*;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = CommandSetWorldSpawn.class, remap = false)
public abstract class MixinCommandSetWorldSpawn {
    /* TODO: Better solution */
    public void execute(MinecraftServer server, ICommandListener sender, String[] args) throws CommandException {
        sender.sendMessage(new ChatComponentText("Broken :( Use /orion:setworldspawn instead."));
    }
}
