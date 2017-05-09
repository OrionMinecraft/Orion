package eu.mikroskeem.orion.api.items;

import eu.mikroskeem.orion.api.interfaces.BukkitItemStack;
import org.bukkit.Material;

import java.util.Collection;

/**
 * @author Mark Vainomaa
 */
public interface ItemStack extends BukkitItemStack {
    /**
     * Set materials what given {@link org.bukkit.inventory.ItemStack} can destroy
     *
     * @param materials Set of materials
     */
    void setCanDestroy(Collection<Material> materials);

    /**
     * Get set of materials where given {@link org.bukkit.inventory.ItemStack} can destroy
     *
     * @return Set of materials
     */
    Collection<Material> getCanDestroy();

    /**
     * Set materials what given {@link org.bukkit.inventory.ItemStack} can be placed on
     *
     * @param materials Set of materials
     */
    void setCanPlaceOn(Collection<Material> materials);

    /**
     * Get set of materials where given {@link org.bukkit.inventory.ItemStack} can be placed on
     *
     * @return Set of materials
     */
    Collection<Material> getCanPlaceOn();
}
