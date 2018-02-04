package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.chest.CustomChestInventoryHolder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * @author xXAndrew28Xx
 */
public class ChestHopperListener extends MoreStorageListener {
    public ChestHopperListener(MoreStorage moreStorage) {
        super(moreStorage);
    }

    // Make chests around hopper start their hopper task
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) {
            return;
        }
        for (BlockFace face : BlockFace.values()) {
            Block targetBlock = block.getRelative(face);
            CustomChest chest = moreStorage.getChestMap().get(targetBlock.getLocation());
            if (chest != null) {
                chest.startHopperTaskIfNeeded(moreStorage);
            }
        }
    }

    // Make sure that items that are supposed to go into our custom chests
    // don't go into the underlying chest
    @EventHandler(priority = EventPriority.LOWEST)
    public void onMoveItem(InventoryMoveItemEvent event) {
        InventoryHolder holder = event.getDestination().getHolder();
        if (shouldCancelMoveItem(holder)) {
            event.setCancelled(true);
        }
    }

    private boolean shouldCancelMoveItem(InventoryHolder holder) {
        if (holder instanceof CustomChestInventoryHolder) {
            CustomChestInventoryHolder customHolder = (CustomChestInventoryHolder) holder;
            if (customHolder.isItemWasJustInsertedByHopper()) {
                customHolder.setItemWasJustInsertedByHopper(false);
            } else {
                return true;
            }
        } else if (holder instanceof Chest) {
            Chest chest = (Chest) holder;
            Block block = chest.getBlock();
            CustomChest customChest = moreStorage.getChestMap().get(block.getLocation());
            return customChest != null;
        } else if (holder instanceof DoubleChest) {
            DoubleChest chest = (DoubleChest) holder;
            return shouldCancelMoveItem(chest.getLeftSide()) || shouldCancelMoveItem(chest.getRightSide());
        }
        return false;
    }
}
