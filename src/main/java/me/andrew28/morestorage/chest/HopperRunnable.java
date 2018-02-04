package me.andrew28.morestorage.chest;

import me.andrew28.morestorage.MoreStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Hopper;

/**
 * A runnable used internally to allow hoppers to work properly on {@link CustomChest}s
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
class HopperRunnable implements Runnable {
    private CustomChest chest;
    private MoreStorage moreStorage;
    private Block block;
    private Inventory inventory;

    HopperRunnable(CustomChest chest, MoreStorage moreStorage) {
        this.chest = chest;
        this.moreStorage = moreStorage;
        this.block = chest.getBlock();
        this.inventory = chest.getInventory();
    }

    @Override
    public void run() {
        if (!chest.isRunning() || !isTouchingHopper() || !moreStorage.isSupportHoppers()) {
            chest.hopperTask.cancel();
            chest.hopperTask = null;
            return;
        }
        // Attempts to take item from hopper to custom chest
        checkInsert();
        // Attempts to take item from custom chest to hopper below
        checkRemove();
    }

    private boolean isTouchingHopper() {
        for (BlockFace face : new BlockFace[]{
                BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH,
                BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            if (block.getRelative(face).getType() == Material.HOPPER) {
                return true;
            }
        }
        return false;
    }

    private void checkInsert() {
        // All block faces that touch the chest except for BlockFace.DOWN may insert items
        for (BlockFace face : new BlockFace[]{
                BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH,
                BlockFace.WEST, BlockFace.UP}) {
            Block hopperBlock = block.getRelative(face);
            if (hopperBlock.getType() != Material.HOPPER
                    || hopperBlock.isBlockPowered()
                    || hopperBlock.isBlockIndirectlyPowered()) {
                continue;
            }
            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) hopperBlock.getState();
            Inventory hopperInventory = hopper.getInventory();
            Hopper hopperData = (Hopper) hopperBlock.getState().getData();
            if (hopperData.getFacing() != face.getOppositeFace()) {
                continue;
            }
            // Find a slot in the hopper to take from
            for (int hopperSlot = hopperInventory.getSize() - 1; hopperSlot > -1; hopperSlot--) {
                ItemStack currentItem = hopperInventory.getItem(hopperSlot);
                if (currentItem == null) {
                    continue;
                }

                // itemToAdd has an amount of 1 because a hopper pushes 1 item at a time
                ItemStack itemToAdd = currentItem.clone();
                itemToAdd.setAmount(1);

                // Check the item through the itemFilter if applicable
                ItemFilter itemFilter = chest.getInfo().getItemFilter();
                if (itemFilter != null && !itemFilter.canPass(itemToAdd.getType())) {
                    continue;
                }

                CustomChestInfo info = chest.getInfo();
                // If the size is specified by slots, then it may not be a multiple of 9
                if (info.getSizeType() == CustomChestInfo.SizeType.SLOTS) {
                    // Test on a fake inventory first to make sure the item doesn't go into a banned slot
                    Inventory clone = Bukkit.createInventory(null, inventory.getSize());
                    for (int slot = 0; slot < inventory.getSize(); slot++) {
                        clone.setItem(slot, inventory.getItem(slot));
                    }
                    clone.addItem(currentItem);
                    int bannedSlotAmount = (info.getRequiredRows() * 9) - info.getSize();
                    int firstBannedSlot = inventory.getSize() - bannedSlotAmount;
                    for (int slot = firstBannedSlot; slot < clone.getSize(); slot++) {
                        if (clone.getItem(slot) != null) {
                            return;
                        }
                    }
                }

                // Call the event like a normal hopper does
                ((CustomChestInventoryHolder) inventory.getHolder()).setItemWasJustInsertedByHopper(true);
                InventoryMoveItemEvent event = new InventoryMoveItemEvent(hopperInventory, itemToAdd,
                        hopperInventory, true);
                moreStorage.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }

                // Inventory#addItem returns a Map with items that didn't fit
                // So if the returned map's size is 0, the item fit and went in
                if (inventory.addItem(itemToAdd).size() == 0) {
                    // If the amount is 1, instead of setting the amount to 0, remove the item instead
                    if (currentItem.getAmount() == 1) {
                        hopperInventory.setItem(hopperSlot, null);
                    } else {
                        currentItem.setAmount(currentItem.getAmount() - 1);
                    }

                }
                break;
            }
        }
    }

    private void checkRemove() {
        // Check the block below as
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        if (blockBelow.getType() != Material.HOPPER
                || blockBelow.isBlockPowered()
                || blockBelow.isBlockIndirectlyPowered()) {
            return;
        }
        org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) blockBelow.getState();
        Inventory hopperInventory = hopper.getInventory();

        // Loop our items backwards as a hopper takes items starting at the last one
        ItemStack[] contents = inventory.getContents();
        for (int slot = contents.length - 1; slot > -1; slot--) {
            ItemStack currentItem = contents[slot];
            if (currentItem == null) {
                continue;
            }

            // itemToAdd has an amount of 1 because a hopper pulls 1 item at a time
            ItemStack itemToAdd = currentItem.clone();
            itemToAdd.setAmount(1);

            // Call the event like a normal hopper does
            InventoryMoveItemEvent event = new InventoryMoveItemEvent(inventory, itemToAdd,
                    hopperInventory, false);
            moreStorage.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            // Inventory#addItem returns a Map with items that didn't fit
            // So if the returned map's size is 0, the item fit and went in
            if (hopperInventory.addItem(itemToAdd).size() == 0) {
                // If the amount is 1, instead of setting the amount to 0, remove the item instead
                if (currentItem.getAmount() == 1) {
                    inventory.setItem(slot, null);
                } else {
                    currentItem.setAmount(currentItem.getAmount() - 1);
                }
            }
            break;
        }
    }
}
