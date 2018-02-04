package me.andrew28.morestorage.chest;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.listeners.WorldChunkListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a placed down custom chest block (not necessarily of type {@link org.bukkit.Material#CHEST}}.
 *
 * @author xXAndrew28Xx
 * @since 0.0.1
 */
public class CustomChest {
    protected BukkitTask hopperTask;
    private CustomChestInfo info;
    private Block block;
    private Inventory inventory;
    private boolean running = true;

    CustomChest(CustomChestInfo info, Block block, Inventory inventory) {
        this.info = info;
        this.block = block;
        this.inventory = inventory;
    }

    /**
     * Get the info for this chest
     *
     * @return The info for this chest
     */
    public CustomChestInfo getInfo() {
        return info;
    }

    /**
     * Sets the info of this chest, resizing the inventory to the new info.
     *
     * @param info the new info to set this chest to use
     */
    public void setInfo(CustomChestInfo info) {
        this.info = info;

        // Resize inventory and remove excess items (if the size isn't a multiple of 9)

        ItemStack[] oldContents = this.inventory.getContents();
        this.inventory = info.createInventory(this);
        int lastSlot = oldContents.length;
        // If there are banned slots (from a size that isn't a multiple of 9),
        // then set the lastSlot to the last unbanned slot
        if (info.getSizeType() == CustomChestInfo.SizeType.SLOTS) {
            int bannedSlotAmount = (info.getRequiredRows() * 9) - info.getSize();
            lastSlot = inventory.getSize() - bannedSlotAmount - 1;
        }
        for (int slot = 0; slot < lastSlot; slot++) {
            inventory.setItem(slot, oldContents[slot]);
        }
    }

    /**
     * Get the block of this chest
     *
     * @return the block of this chest
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Get the inventory of this chest
     *
     * @return the inventory of this chest
     */
    public Inventory getInventory() {
        return inventory;
    }

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Destroy this chest, dropping items.
     *
     * @param worldChunkListener The currently used {@link WorldChunkListener}
     */
    public void destroy(WorldChunkListener worldChunkListener) {
        running = false;
        Location location = block.getLocation();
        worldChunkListener.changedChunks.add(location.getChunk());
        if (inventory != null) {
            Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull)
                    .filter(itemStack -> itemStack.getType() != Material.AIR)
                    .forEach(itemStack -> location.getWorld().dropItemNaturally(location, itemStack));
        }
    }

    /**
     * Get whether the chest's tasks are currently running
     *
     * @return whether the chest's task are currently running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Start the hopper task if needed
     *
     * @param moreStorage The instance of {@link MoreStorage}
     */
    public void startHopperTaskIfNeeded(MoreStorage moreStorage) {
        if (!info.isAllowHoppers()) {
            return;
        }
        // Check if already started
        if (hopperTask != null) {
            return;
        }

        hopperTask = Bukkit.getScheduler().runTaskTimer(moreStorage,
                new HopperRunnable(this, moreStorage), 0, moreStorage.getHopperSpeed());
    }
}
