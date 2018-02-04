package me.andrew28.morestorage.save;

import me.andrew28.morestorage.ChestsLoader;
import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.util.ItemUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Used internally to save (via serialization) chest data
 *
 * @author xXAndrew28Xx
 */
public class ChestData implements Serializable {
    private static final long serialVersionUID = 2970372927922726215L;
    private int x;
    private int absoluteY;
    private int z;
    private String chestId;
    private Map<Integer, byte[]> itemMap;

    public ChestData() {
    }

    private ChestData(int x, int absoluteY, int z, String chestId, Map<Integer, byte[]> itemMap) {
        this.x = x;
        this.absoluteY = absoluteY;
        this.z = z;
        this.chestId = chestId;
        this.itemMap = itemMap;
    }

    public static ChestData fromCustomChest(CustomChest customChest, ItemUtil itemUtil) {
        Block block = customChest.getBlock();
        Chunk chunk = block.getChunk();
        int x = block.getX() - (chunk.getX() * 16);
        int y = block.getY();
        int z = block.getZ() - (chunk.getZ() * 16);
        String id = customChest.getInfo().getId();

        Inventory inventory = customChest.getInventory();
        Map<Integer, byte[]> itemMap = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null) {
                try {
                    itemMap.put(i, itemUtil.serializeItemStack(itemStack));
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ChestData(x, y, z, id, itemMap);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }

    public void setAbsoluteY(int absoluteY) {
        this.absoluteY = absoluteY;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getChestId() {
        return chestId;
    }

    public void setChestId(String chestId) {
        this.chestId = chestId;
    }

    public Map<Integer, byte[]> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, byte[]> itemMap) {
        this.itemMap = itemMap;
    }

    public CustomChest toCustomChest(MoreStorage moreStorage, Chunk chunk) {
        World world = chunk.getWorld();
        ChestsLoader chestsLoader = moreStorage.getChestsLoader();

        int x = this.x + (chunk.getX() * 16);
        int y = this.absoluteY;
        int z = this.z + (chunk.getZ() * 16);
        Block block = world.getBlockAt(x, y, z);

        Optional<CustomChestInfo> infoOptional = chestsLoader.getCustomChestInfoById(this.chestId);
        if (!infoOptional.isPresent()) {
            moreStorage.getLogger().warning("Unknown chest id " + this.chestId + " while loading chest " + block);
            return null;
        }
        CustomChestInfo info = infoOptional.get();
        CustomChest customChest = info.createChest(block);

        Inventory inventory = customChest.getInventory();
        for (Map.Entry<Integer, byte[]> entry : this.itemMap.entrySet()) {
            ItemStack itemStack;
            try {
                itemStack = moreStorage.getItemUtil().deserializeItemStack(entry.getValue());
            } catch (ReflectiveOperationException e) {
                moreStorage.getLogger().warning("Failed to deserialize item while loading chest at " + block);
                e.printStackTrace();
                continue;
            }
            inventory.setItem(entry.getKey(), itemStack);
        }

        return customChest;
    }
}
