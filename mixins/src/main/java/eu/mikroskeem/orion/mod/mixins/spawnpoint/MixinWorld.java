package eu.mikroskeem.orion.mod.mixins.spawnpoint;

import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.World;
import net.minecraft.server.v1_11_R1.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Mark Vainomaa
 */
@Mixin(value = World.class, remap = false)
public abstract class MixinWorld {
    @Shadow public WorldData worldData;

    @Inject(method = "A", cancellable = true, at = @At("HEAD"))
    public void onSetSpawn(BlockPosition blockposition, CallbackInfo cb) {
        new Throwable("World -> setSpawn(). Please report this to @mikroskeem").printStackTrace();
        cb.cancel();
    }
}
