package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.ChestsLoader;
import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.util.NBTUtil;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author xXAndrew28Xx
 */
abstract class MoreStorageListener implements Listener {
    MoreStorage moreStorage;
    Logger logger;
    ChestsLoader chestsLoader;
    Map<Location, CustomChest> chestLocationMap;
    NBTUtil nbtUtil;

    MoreStorageListener(MoreStorage moreStorage) {
        this.moreStorage = moreStorage;
        this.logger = moreStorage.getLogger();
        this.chestsLoader = moreStorage.getChestsLoader();
        this.chestLocationMap = moreStorage.getChestMap();
        this.nbtUtil = moreStorage.getNbtUtil();
    }
}
