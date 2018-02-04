package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.recipe.CustomRecipe;
import me.andrew28.morestorage.recipe.ShapedCustomRecipe;
import me.andrew28.morestorage.recipe.ShapelessCustomRecipe;
import me.andrew28.morestorage.util.ItemMetaUtil;
import me.andrew28.morestorage.util.ListUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author xXAndrew28Xx
 */
public class ChestRecipeListener extends MoreStorageListener {
    public ChestRecipeListener(MoreStorage moreStorage) {
        super(moreStorage);
    }

    // Verify the the ItemMeta of the recipe items are correct
    // TODO : Make code less spaghetti
    @EventHandler
    public void onCraftPrepare(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        CraftingInventory inventory = event.getInventory();
        if (recipe == null || inventory == null || chestsLoader == null
                || (inventory.getType() != InventoryType.WORKBENCH
                && inventory.getType() != InventoryType.CRAFTING)) {
            return;
        }
        ItemStack[] matrix = inventory.getMatrix();
        Optional<CustomChestInfo> customChestOptional = chestsLoader.getCustomChestInfo(recipe.getResult());
        if (!customChestOptional.isPresent()) {
            return;
        }
        CustomChestInfo customChestInfo = customChestOptional.get();

        /* Check if recipe matches any custom recipes for the chest (including ItemMeta!!) */
        boolean anyMatches = false;
        for (CustomRecipe customRecipe : customChestInfo.getRecipes()) {
            if (customRecipe.getType().equals(CustomRecipe.CustomRecipeType.SHAPED)
                    && recipe instanceof ShapedRecipe) {
                // 2 (2x2=4) for Inventory Crafting, 3 (3x3=9) for Workbench Crafting
                int craftingSize = matrix.length == 4 ? 2 : 3;
                List<List<ItemStack>> squareMatrixList = new ArrayList<>();
                for (int row = 0; row < craftingSize; row++) {
                    List<ItemStack> squareMatrixRow = new ArrayList<>();
                    int offset = row * craftingSize;
                    boolean hasNonNull = false;
                    for (int column = 0; column < craftingSize; column++) {
                        ItemStack itemStack = matrix[offset + column];
                        if (itemStack != null) {
                            hasNonNull = true;
                        }
                        squareMatrixRow.add(itemStack);
                    }
                    if (hasNonNull) {
                        squareMatrixList.add(squareMatrixRow);
                    }
                }
                ItemStack[][] squareMatrix = ListUtil.toArray(squareMatrixList, ItemStack.class, ItemStack[].class);
                ItemStack[][] recipeSquareMatrix = ((ShapedCustomRecipe) customRecipe).getMatrix();
                boolean matches = true;
                // Check if arrays are equal, Arrays.equals isn't working for some reason
                check:
                {
                    for (int index = 0; index < squareMatrix.length; index++) {
                        ItemStack[] craftRow = squareMatrix[index];
                        ItemStack[] recipeRow = recipeSquareMatrix[index];
                        if (craftRow.length != recipeRow.length) {
                            matches = false;
                            break check;
                        }
                        for (int innerIndex = 0; innerIndex < craftRow.length; innerIndex++) {
                            if (!ItemMetaUtil.checkBasicMetaMatches(recipeRow[innerIndex], craftRow[innerIndex])) {
                                matches = false;
                                break check;
                            }
                        }
                    }
                }
                if (matches) {
                    anyMatches = true;
                    break;
                }
            } else if (customRecipe.getType().equals(CustomRecipe.CustomRecipeType.SHAPELESS)
                    && recipe instanceof ShapelessRecipe) {
                ItemStack[] customRecipeItems = ((ShapelessCustomRecipe) customRecipe).getItemStacks();
                if (Arrays.asList(matrix).containsAll(Arrays.asList(customRecipeItems))) {
                    anyMatches = true;
                    break;
                }
                boolean containsAllItems = true;
                for (ItemStack recipeItem : customRecipeItems) {
                    boolean matches = false;
                    for (ItemStack craftingItem : matrix) {
                        if (ItemMetaUtil.checkBasicMetaMatches(recipeItem, craftingItem)) {
                            matches = true;
                            break;
                        }
                    }
                    if (!matches) {
                        containsAllItems = false;
                        break;
                    }
                }
                if (containsAllItems) {
                    anyMatches = true;
                    break;
                }
            }
        }

        // No recipes for the chest
        if (!anyMatches) {
            cancel(event);
            return;
        }

        /* Check Permissions */
        List<HumanEntity> viewers = event.getViewers();
        String permission = customChestInfo.getPermission();
        if (permission != null && viewers.size() >= 1) {
            HumanEntity viewer = viewers.get(0);
            if (!viewer.hasPermission(permission)) {
                cancel(event);
            }
        }
    }

    private void cancel(PrepareItemCraftEvent event) {
        event.getInventory().setResult(null);
    }
}
