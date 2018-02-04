package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Iterator;
import java.util.List;

/**
 * @author xXAndrew28Xx
 */
public class ChestExplodeListener extends MoreStorageListener {
    private WorldChunkListener worldChunkListener;

    public ChestExplodeListener(MoreStorage moreStorage, WorldChunkListener worldChunkListener) {
        super(moreStorage);
        this.worldChunkListener = worldChunkListener;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blockList) {
        Iterator<Block> blockIterator = blockList.iterator();
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            Location location = block.getLocation();
            CustomChest chest = moreStorage.getChestMap().get(location);
            if (chest == null) {
                continue;
            }
            // If the chest is blast proof, remove it from the list of blocks
            if (chest.getInfo().isBlastProof()) {
                blockIterator.remove();
            } else {
                // Oh no, it exploded
                moreStorage.getChestMap().remove(location);
                Bukkit.getScheduler().runTaskLater(moreStorage, () -> chest.destroy(worldChunkListener), 1L);
            }
        }
    }

}
