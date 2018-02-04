package me.andrew28.morestorage.recipe;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

/**
 * Represents a recipe where the items do not need to follow a certain shape/pattern
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
public class ShapelessCustomRecipe extends CustomRecipe {
    private ItemStack[] itemStacks;
    private Plugin plugin;
    private String key;

    /**
     * Creates a new shapeless recipe
     *
     * @param result     The result of crafting
     * @param plugin     The plugin
     * @param key        The unique key
     * @param itemStacks The required items
     */
    public ShapelessCustomRecipe(ItemStack result, Plugin plugin, String key, ItemStack[] itemStacks) {
        super(CustomRecipeType.SHAPELESS, result);
        this.itemStacks = itemStacks;
        this.plugin = plugin;
        this.key = key;
    }

    /**
     * Gets the required items
     *
     * @return The required items
     */
    public ItemStack[] getItemStacks() {
        return itemStacks;
    }

    /**
     * Gets the plugin that made this recipe
     *
     * @return The plugin that made this recipe
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Gets the unique key of this recipe
     *
     * @return The unique key of this recipe
     */
    public String getKey() {
        return key;
    }

    @Override
    public void register() {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, result);
        for (ItemStack itemStack : itemStacks) {
            recipe.addIngredient(itemStack.getType());
        }
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("Could not register duplicate recipe: " + namespacedKey);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ShapelessCustomRecipe)) return false;

        ShapelessCustomRecipe that = (ShapelessCustomRecipe) o;

        return new EqualsBuilder()
                .append(itemStacks, that.itemStacks)
                .append(key, that.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(itemStacks)
                .append(key)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("itemStacks", itemStacks)
                .append("key", key)
                .toString();
    }
}
