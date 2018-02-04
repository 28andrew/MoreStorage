package me.andrew28.morestorage.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.Iterator;

/**
 * Represents a recipe
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
public abstract class CustomRecipe {
    ItemStack result;
    private CustomRecipeType type;

    CustomRecipe(CustomRecipeType type, ItemStack result) {
        this.type = type;
        this.result = result;
    }

    /**
     * Gets the type of this recipe
     *
     * @return The type of this recipe
     */
    public CustomRecipeType getType() {
        return type;
    }

    void removeRecipe(Iterator<Recipe> iterator, NamespacedKey namespacedKey) {
        try {
            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();
                if (recipe instanceof ShapedRecipe && ((ShapedRecipe) recipe).getKey().equals(namespacedKey)
                        || recipe instanceof ShapelessRecipe && ((ShapelessRecipe) recipe).getKey().equals(namespacedKey)) {
                    iterator.remove();
                    return;
                }
            }
        } catch (UnsupportedOperationException e) {
            // It isn't possible to remove recipes with the iterator in 1.12 sadly
        }
    }

    /**
     * Registers the recipe to the {@link org.bukkit.Server}
     */
    public abstract void register();

    public enum CustomRecipeType {
        SHAPELESS, SHAPED
    }
}
