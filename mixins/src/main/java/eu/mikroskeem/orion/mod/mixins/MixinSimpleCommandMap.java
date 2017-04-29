package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.OrionServerCore;
import eu.mikroskeem.orion.api.Orion;
import eu.mikroskeem.orion.api.OrionServer;
import eu.mikroskeem.orion.api.server.Configuration;
import eu.mikroskeem.orion.internal.commands.OrionCommand;
import eu.mikroskeem.orion.internal.commands.SetWorldSpawnCommand;
import eu.mikroskeem.shuriken.common.SneakyThrow;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(SimpleCommandMap.class)
public abstract class MixinSimpleCommandMap {
    @Shadow(remap = false) public abstract boolean register(String fallbackPrefix, Command command);

    @Inject(remap = false, method = "setDefaultCommands()V", at = @At(remap = false, value = "HEAD"))
    public void onSetDefaultCommands(CallbackInfo callbackInfo){
        register("orion", new OrionCommand("orion"));
        register("orion", new SetWorldSpawnCommand("setworldspawn"));
    }

    @Redirect(remap = false, method = "dispatch", at = @At(remap = false,
            value = "INVOKE",
            target = "Lorg/bukkit/command/Command;execute(Lorg/bukkit/command/CommandSender;" +
                    "Ljava/lang/String;[Ljava/lang/String;)Z"
    ))
    public boolean executeProxy(Command command, CommandSender sender, String commandLabel, String[] args) {
        OrionServer server = Orion.getServer();
        Configuration configuration = server.getConfiguration();
        try {
            return command.execute(sender, commandLabel, args);
        } catch (Throwable e) {
            if(configuration.getDebug().isReportingCommandExceptionsToSentryAllowed()) {
                ((OrionServerCore) server).getSentryReporter()
                        .reportCommandException(sender, command, commandLabel, args, e);
            }
            SneakyThrow.throwException(e);
        }
        return false; /* Never reaches here */
    }
}
