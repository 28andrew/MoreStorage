package me.andrew28.morestorage.event;

import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a custom chest is placed
 * @author xXAndrew28Xx
 */
public class CustomChestPlaceEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private CustomChest chest;
    private boolean cancelled = false;

    /**
     * Creates a new CustomChestPlaceEvent
     * @param who Who placed the chest
     * @param chest The chest that was placed
     */
    public CustomChestPlaceEvent(Player who, CustomChest chest) {
        super(who);
        this.chest = chest;
    }

    /**
     * Gets the chest that was placed
     * @return The chest that was placed
     */
    public CustomChest getChest() {
        return chest;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
