package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.ChestIdMetadataValue;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.event.CustomChestPlaceEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Optional;

/**
 * @author xXAndrew28Xx
 */
public class ChestPlaceListener extends MoreStorageListener {
    private static final String NO_PERMISSION_MSG_FORMAT =
            ChatColor.RED + "You do not have sufficient permissions to place down the chest %s";
    private WorldChunkListener worldChunkListener;

    public ChestPlaceListener(MoreStorage moreStorage, WorldChunkListener worldChunkListener) {
        super(moreStorage);
        this.worldChunkListener = worldChunkListener;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.canBuild()) {
            return;
        }

        Optional<CustomChestInfo> chestOptional = chestsLoader.getCustomChestInfo(event.getItemInHand());
        if (!chestOptional.isPresent()) {
            return;
        }
        CustomChestInfo chestInfo = chestOptional.get();

        /* Check permission */
        Player player = event.getPlayer();
        String permission = chestInfo.getPermission();
        if (permission != null && !permission.isEmpty() && !player.hasPermission(permission)) {
            event.setCancelled(true);
            player.sendMessage(String.format(NO_PERMISSION_MSG_FORMAT, chestInfo.getName()));
            return;
        }

        Block block = event.getBlock();

        /* Check Double Chest making */
        doubling:
        {
            if (!chestInfo.isAllowDoubling()) {
                Material thisMaterial = chestInfo.getItemStack().getType();
                if (thisMaterial == null || !thisMaterial.name().toLowerCase().contains("chest")) {
                    break doubling;
                }
                for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                    Block relativeBlock = block.getRelative(face);
                    if (relativeBlock != null
                            && (relativeBlock.getType() == thisMaterial
                            || chestLocationMap.get(relativeBlock.getLocation()) != null)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        CustomChest chest = chestInfo.createChest(event.getBlock());
        CustomChestPlaceEvent customEvent = new CustomChestPlaceEvent(player, chest);
        moreStorage.getServer().getPluginManager().callEvent(customEvent);
        if (customEvent.isCancelled()) {
            return;
        }

        Location location = block.getLocation();
        block.setMetadata("chest_id", new ChestIdMetadataValue(moreStorage, block));
        chestLocationMap.put(location, chest);

        chest.startHopperTaskIfNeeded(moreStorage);

        worldChunkListener.loadedChunks.add(location.getChunk());
        worldChunkListener.changedChunks.add(location.getChunk());
    }
}
