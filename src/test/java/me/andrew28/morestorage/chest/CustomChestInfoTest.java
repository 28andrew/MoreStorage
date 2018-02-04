package me.andrew28.morestorage.chest;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author xXAndrew28Xx
 */
public class CustomChestInfoTest {
    private CustomChestInfo info = new CustomChestInfo("test", "test", null, null,
            CustomChestInfo.SizeType.ROWS, 1, null);

    @Test
    public void testItemStackClone() {
        ItemStack itemStack = new ItemStack(Material.STONE);
        info.setItemStack(itemStack);
        assertTrue("The retrieved ItemStack should be a clone", itemStack != info.getItemStack());
    }

    @Test
    public void testSizeSetting() {
        int size = 8;
        CustomChestInfo.SizeType slots = CustomChestInfo.SizeType.SLOTS;

        info.setSize(size, slots);
        assertEquals(info.getSize(), size);
        assertEquals(info.getSizeType(), slots);
    }

    @Test
    public void testRequiredRows() {
        info.setSize(12, CustomChestInfo.SizeType.SLOTS);
        assertEquals(info.getRequiredRows(), 2);
    }

    @Test
    public void testSlotsFromRows() {
        info.setSize(2, CustomChestInfo.SizeType.ROWS);
        assertEquals(info.getSlots(), 18);
    }
}
