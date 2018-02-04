package me.andrew28.morestorage.event;

import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * The event called when a custom chest is destroyed
 * @author xXAndrew28Xx
 */
public class CustomChestDestroyEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private CustomChest chest;
    private DestroyCause cause;
    private boolean cancelled = false;

    /**
     * Creates a new CustomChestDestroyEvent, where a chest has been destroyed
     * @param chest The chest that has been destroyed
     * @param cause The cause of the destruction
     */
    public CustomChestDestroyEvent(CustomChest chest, DestroyCause cause) {
        this.chest = chest;
        this.cause = cause;
    }

    /**
     * Gets the chest that has been destroyed
     * @return The chest that has been destroyed
     */
    public CustomChest getChest() {
        return chest;
    }

    /**
     * Gets the cause of the destruction
     * @return The cause of the destruction
     */
    public DestroyCause getCause() {
        return cause;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Represents a chest destruction cause
     */
    public enum DestroyCause {
        /**
         * Represents when a chest is destructed because of an explosion (either block or entity)
         */
        EXPLOSION,
        /**
         * Represents when a chest is destructed because of a block break
         */
        BLOCK_BREAK
    }
}
