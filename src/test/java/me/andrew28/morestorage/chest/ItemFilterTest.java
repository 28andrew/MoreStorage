package me.andrew28.morestorage.chest;

import org.bukkit.Material;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author xXAndrew28Xx
 */
public class ItemFilterTest {
    private List<Material> someFood = Arrays.asList(
            Material.COOKIE, Material.BREAD, Material.COOKED_BEEF, Material.APPLE,
            Material.POTATO, Material.BAKED_POTATO, Material.GRILLED_PORK);

    @Test
    public void testWhitelist() {
        ItemFilter whitelist = new ItemFilter(true, someFood);
        for (Material food : someFood) {
            assertTrue("The material " + food.name() + " should pass through the whitelist",
                    whitelist.canPass(food) == someFood.contains(food));
        }
    }

    @Test
    public void testBlacklist() {
        ItemFilter blackList = new ItemFilter(false, someFood);
        for (Material food : Material.values()) {
            assertFalse("The material " + food.name() + " shouldn't pass through the blacklist",
                    blackList.canPass(food) == someFood.contains(food));
        }
    }
}
