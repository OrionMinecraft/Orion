package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.items.ItemStack;
import eu.mikroskeem.shuriken.common.Ensure;
import lombok.NonNull;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack.asNMSCopy;

/**
 * @author Mark Vainomaa
 */
@Mixin(CraftItemStack.class)
public abstract class MixinCraftItemStack implements ItemStack {
    @Shadow(remap = false) net.minecraft.server.v1_11_R1.ItemStack handle;

    @Override
    public void setCanDestroy(@NonNull Collection<Material> materials) {
        /* Get tag */
        NBTTagCompound tag;
        if((tag = handle.getTag()) == null) tag = new NBTTagCompound();

        /* Set 'CanBreak' list */
        NBTTagList canDestroy = new NBTTagList();
        new HashSet<>(materials).stream()
                .map(this::orion$getItemId)
                .map(NBTTagString::new)
                .forEach(canDestroy::add);
        tag.set("CanDestroy", canDestroy);

        /* Apply tag */
        handle.setTag(tag);
    }

    @Override
    public Collection<Material> getCanDestoy() {
        /* Get tag */
        NBTTagCompound tag;
        if((tag = handle.getTag()) == null) tag = new NBTTagCompound();

        /* Try to get list 'canDestroy' */
        NBTTagList canDestroy = tag.getList("CanDestroy", (byte)8);
        if(canDestroy != null && canDestroy.size() > 0) {
            return canDestroy.list.stream()
                    .map(nbtBase -> (NBTTagString)nbtBase)
                    .map(NBTTagString::c_)
                    .map(Item::b)
                    .map(CraftItemStack::asNewCraftStack)
                    .map(org.bukkit.inventory.ItemStack::getType)
                    .collect(Collectors.toSet());
        }

        /* Return empty set otherwise */
        return Collections.emptySet();
    }

    @Override
    public void setCanPlaceOn(@NonNull Collection<Material> materials) {
        /* Get tag */
        NBTTagCompound tag;
        if((tag = handle.getTag()) == null) tag = new NBTTagCompound();

         /* Set 'CanPlaceOn' list */
        NBTTagList canPlaceOn = new NBTTagList();
        new HashSet<>(materials).stream()
                .map(this::orion$getItemId)
                .map(NBTTagString::new)
                .forEach(canPlaceOn::add);
        tag.set("CanPlaceOn", canPlaceOn);

        /* Apply tag */
        handle.setTag(tag);
    }

    @Override
    public Collection<Material> getCanPlaceOn() {
        /* Get tag */
        NBTTagCompound tag;
        if((tag = handle.getTag()) == null) tag = new NBTTagCompound();

        /* Try to get list 'canDestroy' */
        NBTTagList canDestroy = tag.getList("CanPlaceOn", (byte)8);
        if(canDestroy != null && canDestroy.size() > 0) {
            return canDestroy.list.stream()
                    .map(nbtBase -> (NBTTagString)nbtBase)
                    .map(NBTTagString::c_)
                    .map(Item::b)
                    .map(CraftItemStack::asNewCraftStack)
                    .map(org.bukkit.inventory.ItemStack::getType)
                    .collect(Collectors.toSet());
        }

        /* Return empty set otherwise */
        return Collections.emptySet();
    }

    /* Material.DIAMOND_ORE -> minecraft:diamond_ore */
    private String orion$getItemId(Material material) {
        net.minecraft.server.v1_11_R1.ItemStack breakableItemStack = asNMSCopy(new org.bukkit.inventory.ItemStack(material, 1));
        MinecraftKey minecraftKey = Ensure.notNull(Item.REGISTRY.b(breakableItemStack.getItem()), "");
        return minecraftKey.toString();
    }
}
