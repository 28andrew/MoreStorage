package me.andrew28.morestorage.save;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.ChestIdMetadataValue;
import me.andrew28.morestorage.chest.CustomChest;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used internally to hold multiple {@link ChestData}s for saving (serialization)
 *
 * @author xXAndrew28Xx
 */
public class ChunkChestData implements Serializable {
    private static final long serialVersionUID = -1368428718615154412L;
    private ChestData[] chestData;

    public ChunkChestData() {
    }

    private ChunkChestData(ChestData[] chestData) {
        this.chestData = chestData;
    }

    public static ChunkChestData fromChunk(Chunk chunk, MoreStorage moreStorage) {
        List<ChestData> chestDataList = new ArrayList<>();
        for (CustomChest chest : moreStorage.getChestMap().values()) {
            if (!chest.getBlock().getChunk().equals(chunk)) {
                continue;
            }
            chestDataList.add(ChestData.fromCustomChest(chest, moreStorage.getItemUtil()));
        }
        return new ChunkChestData(chestDataList.toArray(new ChestData[chestDataList.size()]));
    }

    public ChestData[] getChestData() {
        return chestData;
    }

    public void setChestData(ChestData[] chestData) {
        this.chestData = chestData;
    }

    public void load(Chunk chunk, MoreStorage moreStorage) {
        Map<Location, CustomChest> chestMap = moreStorage.getChestMap();
        for (ChestData chestData : this.chestData) {
            CustomChest customChest = chestData.toCustomChest(moreStorage, chunk);
            chestMap.put(customChest.getBlock().getLocation(), customChest);

            Block block = customChest.getBlock();
            block.setMetadata("chest_id", new ChestIdMetadataValue(moreStorage, block));

            customChest.startHopperTaskIfNeeded(moreStorage);
        }
    }
}
