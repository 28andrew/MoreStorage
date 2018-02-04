package me.andrew28.morestorage.event;

import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

/**
 * Called when an action (close/open) happens on a custom chest inventory
 * @author xXAndrew28Xx
 */
public class CustomChestInventoryEvent extends Event implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private CustomChest chest;
    private Inventory inventory;
    private boolean cancelled;

    /**
     * Creates a new CustomChestInventoryEvent
     * @param chest The chest of the inventory of the action
     * @param inventory The inventory of the action
     */
    public CustomChestInventoryEvent(CustomChest chest, Inventory inventory) {
        this.chest = chest;
        this.inventory = inventory;
    }

    /**
     * Gets the chest of the inventory of the action
     * @return The chest of the inventory of the action
     */
    public CustomChest getChest() {
        return chest;
    }

    /**
     * Gets the inventory of the action
     * @return The inventory of action
     */
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
