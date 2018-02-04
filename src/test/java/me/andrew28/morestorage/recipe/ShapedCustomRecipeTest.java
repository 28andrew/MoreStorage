package me.andrew28.morestorage.recipe;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author xXAndrew28Xx
 */
public class ShapedCustomRecipeTest {
    @Test
    public void testMatrix() {
        Map<Character, ItemStack> map = new HashMap<>();
        map.put('A', new ItemStack(Material.APPLE, 1));
        map.put('B', new ItemStack(Material.BAKED_POTATO, 3));
        map.put('C', new ItemStack(Material.CACTUS, 7));
        map.put('D', new ItemStack(Material.DARK_OAK_DOOR, 9));
        map.put('E', new ItemStack(Material.EGG, 4));
        map.put('F', new ItemStack(Material.FEATHER, 14));
        map.put('G', new ItemStack(Material.GHAST_TEAR, 45));
        map.put('H', new ItemStack(Material.HARD_CLAY, 32));
        map.put('I', new ItemStack(Material.ICE, 21));

        String[] pattern = new String[] {
            "ABC",
            "DEF",
            "GHI"
        };

        ShapedCustomRecipe recipe = new ShapedCustomRecipe(null, null, "test", map, pattern);
        ItemStack[][] matrix = recipe.getMatrix();
        for (int row = 0; row < pattern.length; row++) {
            String patternRow = pattern[row];
            for (int column = 0; column < patternRow.length(); column++) {
                char character = patternRow.charAt(column);
                assertEquals(map.get(character), matrix[row][column]);
            }
        }
    }
}
