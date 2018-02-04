package me.andrew28.morestorage.listeners;

import me.andrew28.morestorage.MoreStorage;
import me.andrew28.morestorage.save.ChunkChestData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xXAndrew28Xx
 */
public class WorldChunkListener extends MoreStorageListener {
    private static final String DATA_FOLDER = "data/morestorage";
    private static final String CHEST_CHUNK_NAME_FORMAT = "chests.%d.%d.dat";
    private final Map<Chunk, Integer> chunkUnloadTaskIdMap = new ConcurrentHashMap<>();
    public Set<Chunk> loadedChunks = ConcurrentHashMap.newKeySet();
    public Set<Chunk> changedChunks = ConcurrentHashMap.newKeySet();
    private Map<Chunk, Long> lastLoadedTime = new ConcurrentHashMap<>();

    public WorldChunkListener(MoreStorage moreStorage) {
        super(moreStorage);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        // If the chunk was just generated, there won't be any existing data
        if (event.isNewChunk()) {
            return;
        }

        loadChunk(chunk);
    }

    // Load chunks in a radius of the server's view distance + 2 around the player
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int viewDistance = moreStorage.getServer().getViewDistance() + 2;
        Location location = event.getPlayer().getLocation();
        World world = location.getWorld();
        for (int zPos = location.getBlockZ() + viewDistance; zPos > location.getBlockZ() - viewDistance; zPos -= 16) {
            for (int xPos = location.getBlockX() + viewDistance; xPos > location.getBlockX() - viewDistance; xPos -= 16) {
                loadChunk(new Location(world, xPos, 0, zPos).getChunk());
            }
        }
    }

    private void loadChunk(Chunk chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(moreStorage, () -> {
            if (loadedChunks.contains(chunk)) {
                lastLoadedTime.put(chunk, System.currentTimeMillis());
                return;
            }
            File chestDataFile = getChestDataFile(chunk);

            // If it doesn't exist, it doesn't need to be loaded
            if (!chestDataFile.exists()) {
                return;
            }

            ChunkChestData data;
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(chestDataFile))) {
                data = (ChunkChestData) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                logger.warning("Failed to read chest data from chunk " + chunk + ", possibly corrupted.");
                e.printStackTrace();
                return;
            }

            data.load(chunk, moreStorage);
            loadedChunks.add(chunk);
            lastLoadedTime.put(chunk, System.currentTimeMillis());
        });
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (chunkUnloadTaskIdMap.containsKey(chunk) || !changedChunks.contains(chunk)) {
            return;
        }
        final long start = System.currentTimeMillis();
        // Unload and save changed chunks after 30 seconds
        // if they haven't been loaded again after being unloaded
        int id = Bukkit.getScheduler().scheduleSyncDelayedTask(moreStorage, () -> {
            if (lastLoadedTime.containsKey(chunk) && lastLoadedTime.get(chunk) <= start && !chunk.isLoaded()) {
                saveChunk(chunk, true);

                changedChunks.remove(chunk);
                loadedChunks.remove(chunk);
                lastLoadedTime.remove(chunk);
            }
        }, 20L * 30);
        chunkUnloadTaskIdMap.put(chunk, id);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldSave(WorldSaveEvent event) {
        World world = event.getWorld();
        Iterator<Chunk> iterator = changedChunks.iterator();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            if (!chunk.getWorld().equals(world)) {
                continue;
            }

            saveChunk(chunk, false);

            changedChunks.remove(chunk);
            lastLoadedTime.remove(chunk);

            iterator.remove();
        }
    }

    private void saveChunk(Chunk chunk, boolean unload) {
        Bukkit.getScheduler().runTaskAsynchronously(moreStorage, () -> {
            ChunkChestData data = ChunkChestData.fromChunk(chunk, moreStorage);
            File dataFolder = getDataFolder(chunk.getWorld());
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                logger.warning("Failed to make " + dataFolder.getAbsolutePath());
            }

            File chestDataFile = getChestDataFile(chunk);

            // No data, and file was ever made. So nothing is needed to be done.
            if (data.getChestData().length == 0) {
                if (chestDataFile.exists()) {
                    // If there is no data and the file doesn't exist, nothing needs to be done
                    return;
                } else {
                    if (!chestDataFile.delete()) {
                        logger.warning("Failed to delete " + chestDataFile.getAbsolutePath());
                    }
                }
            }

            try (ObjectOutputStream objectOutputStream =
                         new ObjectOutputStream(new FileOutputStream(chestDataFile, false))) {
                objectOutputStream.writeObject(data);
            } catch (IOException e) {
                logger.warning("Failed to save chest data for chunk " + chunk + "!!");
                e.printStackTrace();
            }

            if (unload) {
                loadedChunks.remove(chunk);
            }
        });
    }

    private File getDataFolder(World world) {
        return new File(world.getWorldFolder(), DATA_FOLDER);
    }

    private File getChestDataFile(Chunk chunk) {
        return new File(getDataFolder(chunk.getWorld()), String.format(CHEST_CHUNK_NAME_FORMAT, chunk.getX(), chunk.getZ()));
    }
}
