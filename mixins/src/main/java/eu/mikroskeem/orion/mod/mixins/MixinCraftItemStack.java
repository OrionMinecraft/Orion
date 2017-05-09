package eu.mikroskeem.orion.mod.mixins;

import eu.mikroskeem.orion.api.items.ItemStack;
import lombok.NonNull;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagList;
import net.minecraft.server.v1_11_R1.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_11_R1.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

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
    public Collection<Material> getCanDestroy() {
        /* Get tag */
        NBTTagCompound tag;
        if((tag = handle.getTag()) == null) tag = new NBTTagCompound();

        /* Try to get list 'canDestroy' */
        NBTTagList canDestroy = tag.getList("CanDestroy", (byte)8);
        if(canDestroy != null && canDestroy.size() > 0) {
            return canDestroy.list.stream()
                    .map(nbtBase -> (NBTTagString)nbtBase)
                    .map(NBTTagString::c_)
                    .map(CraftMagicNumbers.INSTANCE::getMaterialFromInternalName)
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
                    .map(CraftMagicNumbers.INSTANCE::getMaterialFromInternalName)
                    .collect(Collectors.toSet());
        }

        /* Return empty set otherwise */
        return Collections.emptySet();
    }

    /* Material.DIAMOND_ORE -> minecraft:diamond_ore */
    private String orion$getItemId(Material material) {
        return Block.REGISTRY.b(CraftMagicNumbers.getBlock(material)).toString();
    }
}
