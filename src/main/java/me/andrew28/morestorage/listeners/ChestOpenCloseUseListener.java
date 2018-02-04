package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.chest.CustomChestInventoryHolder;
import me.andrew28.morestorage.chest.ItemFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

/**
 * @author xXAndrew28Xx
 */
public class ChestOpenCloseUseListener extends MoreStorageListener {
    private WorldChunkListener worldChunkListener;

    public ChestOpenCloseUseListener(MoreStorage moreStorage, WorldChunkListener worldChunkListener) {
        super(moreStorage);
        this.worldChunkListener = worldChunkListener;
    }

    // Highest so other plugins can handle chest opening logic (e.g locked chests) first
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Location location = block.getLocation();
        CustomChest chest = chestLocationMap.get(location);
        if (chest == null) {
            return;
        }
        CustomChestInfo info = chest.getInfo();

        Player player = event.getPlayer();
        // If player is sneaking, only allow their hand item to be used
        if (player.isSneaking()) {
            event.setUseItemInHand(Event.Result.ALLOW);
            event.setUseInteractedBlock(Event.Result.DENY);
            return;
        }

        // Cancel the event (stops underlying chest from opening) so we can do our own logic
        event.setCancelled(true);

        // Check block above if needed
        if (info.isRequireTopClearance()) {
            Block blockAbove = chest.getBlock().getRelative(BlockFace.UP);
            if (blockAbove != null && blockAbove.getType().isOccluding()) {
                return;
            }
        }

        // Make sure to save this chunk's chest data as the contents of the chest may have changed
        worldChunkListener.changedChunks.add(location.getChunk());

        // Make chest look open
        try {
            moreStorage.getEffectUtil().changeChestOpenState(block, true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        // Open chest inventory to player
        Bukkit.getScheduler()
                .runTaskLater(moreStorage, () -> player.openInventory(chest.getInventory()), 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        CustomChest chest = getChest(inventory);
        if (chest == null) {
            return;
        }
        // If there's only one viewer left, then they're the one that's about to close it
        if (chest.getInventory().getViewers().size() == 1) {
            Block block = chest.getBlock();
            World world = block.getWorld();
            // Play chest close sound
            world.playSound(block.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1, 1);
            // Make chest appear closed now
            try {
                moreStorage.getEffectUtil().changeChestOpenState(block, false);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    // For chests with a slot size that is not a multiple of 9
    // Also checking item filter
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        Inventory inventory = event.getClickedInventory();
        CustomChest chest = getChest(inventory);
        if (chest == null) {
            return;
        }
        CustomChestInfo info = chest.getInfo();

        ItemFilter itemFilter = info.getItemFilter();
        if (itemFilter != null) {
            ItemStack itemStack = null;
            switch (event.getAction()) {
                case PLACE_ALL:
                case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                    itemStack = event.getCursor();
                    break;
                case HOTBAR_SWAP:
                    itemStack = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                    break;
            }
            if (itemStack != null && !itemFilter.canPass(itemStack.getType())) {
                event.setCancelled(true);
                return;
            }
        }

        // We're only looking for chests that have the SizeType of SLOTS
        if (info.getSizeType() == CustomChestInfo.SizeType.SLOTS) {
            // The amount of slots at the end of the last row that can't be used
            int bannedSlotAmount = (info.getRequiredRows() * 9) - info.getSize();
            if (bannedSlotAmount > 0) {
                // Slots are 0-indexed
                int firstBannedSlot = inventory.getSize() - bannedSlotAmount;
                if (slot >= firstBannedSlot) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // For chests with a slot size that is not a multiple of 9
    // Also checking item filter
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory == null) {
            return;
        }
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof CustomChestInventoryHolder)) {
            return;
        }
        CustomChest chest = ((CustomChestInventoryHolder) holder).getCustomChest();
        if (chest == null) {
            return;
        }
        CustomChestInfo info = chest.getInfo();

        // Check if the drag is affecting the chest
        Optional<Integer> slotOptional = event.getRawSlots()
                .stream()
                .filter(i -> i <= (inventory.getSize() - 1)).findAny();
        if (!slotOptional.isPresent()) {
            // Chest not being affected, we don't care!
            return;
        }

        ItemFilter filter = info.getItemFilter();
        if (filter != null) {
            for (ItemStack itemStack : event.getNewItems().values()) {
                if (!filter.canPass(itemStack.getType())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // We're only looking for chests that have the SizeType of SLOTS
        if (info.getSizeType() == CustomChestInfo.SizeType.SLOTS) {
            // The amount of slots at the end of the last row that can't be used
            int bannedSlotAmount = (info.getRequiredRows() * 9) - info.getSize();
            if (bannedSlotAmount > 0) {
                // Slots are 0-indexed
                int firstBannedSlot = inventory.getSize() - bannedSlotAmount;
                int lastChestSlot = inventory.getSize() - 1;
                for (int slot : event.getInventorySlots()) {
                    // Slots outside of the chest may also be included in the drag,
                    // so we have to make sure we don't check those!
                    if (slot <= lastChestSlot && slot >= firstBannedSlot) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    // For chests with a slot size that is not a multiple of 9
    @EventHandler
    public void onMoveItemEvent(InventoryClickEvent event) {
        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
                || !(event.getClickedInventory() instanceof PlayerInventory)) {
            return;
        }
        // InventoryClickEvent doesn't tell us which slots are going to be affected by the move
        // So we have to hackily clone the inventory, then add the original item to it
        // then test ourselves if a banned slot is affected :\

        Inventory inventory = event.getWhoClicked().getOpenInventory().getTopInventory();
        int size = inventory.getSize();
        // If the size isn't a multiple of 9, we don't care about this inventory as it's not a chest
        if (size % 9 != 0) {
            return;
        }
        Inventory clone = Bukkit.createInventory(null, size);

        // Copy to clone
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            clone.setItem(slot, inventory.getItem(slot));
        }

        CustomChest chest = getChest(inventory);
        if (chest == null) {
            return;
        }

        CustomChestInfo info = chest.getInfo();

        ItemStack currentItem = event.getCurrentItem();
        ItemFilter itemFilter = info.getItemFilter();
        if (itemFilter != null && !itemFilter.canPass(currentItem.getType())) {
            event.setCancelled(true);
            return;
        }

        // We're only looking for chests that have the SizeType of SLOTS
        if (info.getSizeType() == CustomChestInfo.SizeType.SLOTS) {
            clone.addItem(currentItem);
            int bannedSlotAmount = (info.getRequiredRows() * 9) - info.getSize();
            int firstBannedSlot = inventory.getSize() - bannedSlotAmount;
            for (int slot = firstBannedSlot; slot < clone.getSize(); slot++) {
                if (clone.getItem(slot) != null) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private CustomChest getChest(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof CustomChestInventoryHolder)) {
            return null;
        }
        return ((CustomChestInventoryHolder) holder).getCustomChest();
    }
}
