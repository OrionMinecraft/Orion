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
@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow(remap = false) public WorldData worldData;

    @Inject(remap = false, method = "A", cancellable = true, at = @At(remap = false, value = "HEAD"))
    public void onSetSpawn(BlockPosition blockposition, CallbackInfo cb) {
        new Throwable("World -> setSpawn(). Please report this to @mikroskeem").printStackTrace();
        cb.cancel();
    }
}
