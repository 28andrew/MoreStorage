package me.andrew28.morestorage.chest;

import me.andrew28.morestorage.MoreStorage;
import org.bukkit.block.Block;
import org.bukkit.metadata.LazyMetadataValue;

/**
 * A metadata value on custom chests that exposes the chest id
 *
 * @author xXAndrew28Xx
 * @since 0.0.1
 */
public class ChestIdMetadataValue extends LazyMetadataValue {
    public ChestIdMetadataValue(MoreStorage moreStorage, final Block block) {
        super(moreStorage, () -> {
            CustomChest chest = moreStorage.getChestMap().get(block.getLocation());
            if (chest == null || chest.getInfo() == null) {
                return null;
            }
            return chest.getInfo().getId();
        });
    }
}
