package eu.mikroskeem.orion.mod.mixins;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.bukkit.event.Event;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Mark Vainomaa
 */
@Mixin(Event.class)
public abstract class MixinEvent {
    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this);
    }
}
