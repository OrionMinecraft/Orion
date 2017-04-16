package eu.mikroskeem.orion.api.interfaces;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.Map;

/**
 * Methods extracted from {@link org.bukkit.inventory.ItemStack}
 *
 * @author Mark Vainomaa
 */
public interface BukkitItemStack {
    Material getType();
    void setType(Material type);
    int getTypeId();
    void setTypeId(int type);
    int getAmount();
    void setAmount(int amount);
    MaterialData getData();
    void setData(MaterialData data);
    void setDurability(short durability);
    short getDurability();
    int getMaxStackSize();
    int getEnchantmentLevel(Enchantment ench);
    Map<Enchantment, Integer> getEnchantments();
    ItemMeta getItemMeta();
    boolean setItemMeta(ItemMeta itemMeta);
}
