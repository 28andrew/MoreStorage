package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.event.CustomChestDestroyEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

/**
 * @author xXAndrew28Xx
 */
public class ChestDestroyListener extends MoreStorageListener {
    private WorldChunkListener worldChunkListener;

    public ChestDestroyListener(MoreStorage moreStorage, WorldChunkListener worldChunkListener) {
        super(moreStorage);
        this.worldChunkListener = worldChunkListener;
    }

    // Handle the breaking of a custom chest
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        Map<Location, CustomChest> chestMap = moreStorage.getChestMap();
        CustomChest chest = chestMap.get(location);
        if (chest == null) {
            return;
        }

        CustomChestDestroyEvent customEvent = new CustomChestDestroyEvent(chest,
                CustomChestDestroyEvent.DestroyCause.BLOCK_BREAK);
        moreStorage.getServer().getPluginManager().callEvent(customEvent);
        if (customEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        chestMap.remove(location);
        chest.destroy(worldChunkListener);
    }
}
