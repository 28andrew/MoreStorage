package me.andrew28.morestorage.event;

import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * The event called when a custom chest inventory is opened
 * @author xXAndrew28Xx
 */
public class CustomChestInventoryOpenEvent extends CustomChestInventoryEvent {
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Creates a new CustomChestInventoryCloseEvent
     * @param chest The closed inventory's chest
     * @param inventory The opened inventory
     */
    public CustomChestInventoryOpenEvent(CustomChest chest, Inventory inventory) {
        super(chest, inventory);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
