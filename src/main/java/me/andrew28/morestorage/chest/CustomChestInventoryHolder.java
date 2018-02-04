package me.andrew28.morestorage.chest;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Represents the holder (a {@link CustomChest}) for the {@link Inventory} that backs a {@link CustomChest}
 *
 * @author xXAndrew28Xx
 */
public class CustomChestInventoryHolder implements InventoryHolder {
    private CustomChest customChest;
    private Inventory inventory;
    private boolean itemWasJustInsertedByHopper = false;

    /**
     * Creates a new inventory holder for a CustomChest
     *
     * @param customChest The custom chest to create the holder for
     * @param inventory   The inventory that the holder holds
     */
    public CustomChestInventoryHolder(CustomChest customChest, Inventory inventory) {
        this.customChest = customChest;
        this.inventory = inventory;
    }

    CustomChestInventoryHolder(CustomChest customChest) {
        this.customChest = customChest;
    }

    /**
     * Gets the custom chest that the inventory of this holder backs
     *
     * @return The custom chest that the inventory of this holder backs
     */
    public CustomChest getCustomChest() {
        return customChest;
    }

    /**
     * Sets the custom chest that the inventory of this holder backs
     *
     * @param customChest The custom chest that the inventory of this holder backs to set to
     */
    public void setCustomChest(CustomChest customChest) {
        this.customChest = customChest;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Gets whether an item was just inserted by a hopper.
     * (Used internally to cancel false {@link org.bukkit.event.inventory.InventoryMoveItemEvent}s)
     *
     * @return Whether an item was just inserted by a hopper
     */
    public boolean isItemWasJustInsertedByHopper() {
        return itemWasJustInsertedByHopper;
    }

    /**
     * Sets whether an item was just inserted by a hopper.
     * (Used internally to cancel false {@link org.bukkit.event.inventory.InventoryMoveItemEvent}s)
     *
     * @param itemWasJustInsertedByHopper Whether an item was just inserted by a hopper
     */
    public void setItemWasJustInsertedByHopper(boolean itemWasJustInsertedByHopper) {
        this.itemWasJustInsertedByHopper = itemWasJustInsertedByHopper;
    }
}
