package me.andrew28.morestorage.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * @author xXAndrew28Xx
 */
public class ItemMetaUtil {
    public static boolean checkBasicMetaMatches(ItemStack first, ItemStack second) {
        if (first == null) {
            return second == null;
        }
        if (!first.getType().equals(second.getType())
                || first.getAmount() != second.getAmount()) {
            return false;
        }
        ItemMeta firstMeta = first.getItemMeta();
        ItemMeta secondMeta = second.getItemMeta();
        return (!firstMeta.hasDisplayName() || Objects.equals(firstMeta.getDisplayName(), secondMeta.getDisplayName()))
                && (!firstMeta.hasLore() || !Objects.equals(firstMeta.getLore(), secondMeta.getLore()));
    }
}
