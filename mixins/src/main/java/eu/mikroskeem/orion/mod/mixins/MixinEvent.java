package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.events.Event;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Mark Vainomaa
 */
@Mixin(org.bukkit.event.Event.class)
public abstract class MixinEvent implements Event {
    @Shadow(remap = false) public abstract boolean isAsynchronous();
    @Shadow(remap = false) @Final private boolean async;

    @Override
    public boolean setAsync(boolean value) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isAsync() {
        return this.isAsynchronous();
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
