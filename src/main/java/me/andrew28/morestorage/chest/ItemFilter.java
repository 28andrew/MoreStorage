package me.andrew28.morestorage.chest;

import org.bukkit.Material;

import java.util.List;

/**
 * Represents a filter that restricts {@link org.bukkit.inventory.ItemStack}s based on a whitelist/blacklist
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
public class ItemFilter {
    private boolean whitelist;
    private List<Material> materialList;

    /**
     * Creates a new ItemFilter
     *
     * @param whitelist    If true, the list of materials represents a whitelist, otherwise a blacklist
     * @param materialList The list of materials
     */
    public ItemFilter(boolean whitelist, List<Material> materialList) {
        this.whitelist = whitelist;
        this.materialList = materialList;
    }

    /**
     * Gets whether a whitelist is being used, otherwise a blacklist is being used.
     *
     * @return whether a whitelist is being used
     */
    public boolean isWhitelist() {
        return whitelist;
    }

    /**
     * Gets the material list that this filter uses as a whitelist/blacklist
     *
     * @return The material list for the whitelist/blacklist
     */
    public List<Material> getMaterialList() {
        return materialList;
    }

    /**
     * Gets whether a material is inside the whitelist/doesn't match the blacklist
     *
     * @param material The material to check against the whitelist/blacklist
     * @return whether a material is inside the whitelist/doesn't match the blacklist
     */
    public boolean canPass(Material material) {
        return whitelist == materialList.contains(material);
    }
}
