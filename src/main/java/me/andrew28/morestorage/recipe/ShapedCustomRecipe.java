package me.andrew28.morestorage.recipe;

import me.andrew28.morestorage.util.ListUtil;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a recipe where the items have to be layed out in a Matrix (usually with a max size of 9x9)
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
public class ShapedCustomRecipe extends CustomRecipe {
    private Map<Character, ItemStack> map;
    private Plugin plugin;
    private String key;
    private String[] pattern;
    private ItemStack[][] matrix;

    /**
     * Creates a new shaped recipe
     *
     * @param result  The result from crafting
     * @param plugin  The plugin
     * @param key     The unique key
     * @param map     The character mapping
     * @param pattern The shape/pattern
     */
    public ShapedCustomRecipe(ItemStack result, Plugin plugin, String key,
                              Map<Character, ItemStack> map, String[] pattern) {
        super(CustomRecipeType.SHAPED, result);
        this.map = map;
        this.plugin = plugin;
        this.key = key;
        this.pattern = pattern;
    }

    /**
     * Gets the mapping of characters in the pattern to ItemStacks
     *
     * @return The mapping of characters in the pattern to ItemStacks
     */
    public Map<Character, ItemStack> getMap() {
        return map;
    }

    /**
     * Gets the plugin that made the recipe
     *
     * @return The plugin that made the recipe
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

    /**
     * Gets the pattern of this recipe
     *
     * @return the pattern of this recipe
     */
    public String[] getPattern() {
        return pattern;
    }

    /**
     * Gets the matrix for this recipe
     *
     * @return A multi-dimensional array that represents the smallest matrix possible for this recipe
     */
    public ItemStack[][] getMatrix() {
        if (matrix == null) {
            List<List<ItemStack>> matrixList = new ArrayList<>();
            for (String rowPattern : pattern) {
                List<ItemStack> squareMatrixRow = new ArrayList<>();
                for (int column = 0; column < rowPattern.length(); column++) {
                    char character = rowPattern.charAt(column);
                    squareMatrixRow.add(map.get(character));
                }
                if (squareMatrixRow.size() >= 1) {
                    matrixList.add(squareMatrixRow);
                }
            }
            matrix = ListUtil.toArray(matrixList, ItemStack.class, ItemStack[].class);
        }
        return matrix;
    }

    @Override
    public void register() {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
        shapedRecipe.shape(pattern);
        for (Map.Entry<Character, ItemStack> entry : map.entrySet()) {
            shapedRecipe.setIngredient(entry.getKey(), entry.getValue().getType());
        }
        try {
            plugin.getServer().addRecipe(shapedRecipe);
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("Could not register duplicate recipe: " + namespacedKey);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof ShapedCustomRecipe)) return false;

        ShapedCustomRecipe that = (ShapedCustomRecipe) o;

        return new EqualsBuilder()
                .append(map, that.map)
                .append(key, that.key)
                .append(pattern, that.pattern)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(map)
                .append(key)
                .append(pattern)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("map", map)
                .append("key", key)
                .append("pattern", pattern)
                .append("result", result)
                .toString();
    }
}
