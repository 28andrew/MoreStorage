package me.andrew28.morestorage;

import me.andrew28.morestorage.chest.CustomChest;
import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.command.ChestCommand;
import me.andrew28.morestorage.command.MainCommand;
import me.andrew28.morestorage.listeners.ChestDestroyListener;
import me.andrew28.morestorage.listeners.ChestExplodeListener;
import me.andrew28.morestorage.listeners.ChestHopperListener;
import me.andrew28.morestorage.listeners.ChestOpenCloseUseListener;
import me.andrew28.morestorage.listeners.ChestPlaceListener;
import me.andrew28.morestorage.listeners.ChestRecipeListener;
import me.andrew28.morestorage.listeners.WorldChunkListener;
import me.andrew28.morestorage.util.EffectUtil;
import me.andrew28.morestorage.util.ItemUtil;
import me.andrew28.morestorage.util.Messages;
import me.andrew28.morestorage.util.NBTUtil;
import me.andrew28.morestorage.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * The MoreStorage Plugin
 *
 * @author xXAndrew28Xx
 */
public class MoreStorage extends JavaPlugin {
    private ReflectionUtil reflectionUtil;
    private NBTUtil nbtUtil;
    private ItemUtil itemUtil;
    private EffectUtil effectUtil;

    private boolean resetRecipes, supportHoppers;
    private int hopperSpeed;

    private File chestsFile = new File(getDataFolder(), "chests.yml");
    private ChestsLoader chestsLoader;
    private Map<Location, CustomChest> chestMap = new ConcurrentHashMap<>();

    private File messagesFile = new File(getDataFolder(), "messages.yml");
    private Messages messages;

    @Override
    public void onEnable() {
        // Retrieve NMS version
        String serverVersion = getServer().getClass().getPackage().getName().split("\\.")[3];
        getLogger().info("Detected NMS version: " + serverVersion);

        // Initialize Utilities
        reflectionUtil = new ReflectionUtil(serverVersion);
        try {
            nbtUtil = new NBTUtil(reflectionUtil);
            itemUtil = new ItemUtil(nbtUtil);
            effectUtil = new EffectUtil(reflectionUtil);
        } catch (ReflectiveOperationException e) {
            disable("Failed to initialize NMS", e);
            return;
        }

        // Standard Configuration
        saveDefaultConfig();
        readConfig();
        // Chests Configuration
        if (!chestsFile.exists()) {
            saveResource("chests.yml", false);
        }
        chestsLoader = new ChestsLoader(chestsFile, this);
        chestsLoader.load();
        // Messages
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = new Messages(messagesFile);
        messages.load();
        // Register listeners
        registerListeners();
        // Register commands
        getCommand("morestorage").setExecutor(new MainCommand(this));
        getCommand("customchest").setExecutor(new ChestCommand(this));
    }

    private void reloadChests() {
        chestsLoader.load();
    }

    private void reloadMessages() {
        messages.load();
    }

    private void readConfig() {
        reloadConfig();
        resetRecipes = getConfig().getBoolean("reset-recipes", false);
        supportHoppers = getConfig().getBoolean("hopper-support", true);
        hopperSpeed = getConfig().getInt("hopper-speed", 8);
    }

    private void redoChests() {
        chestMap.values().forEach(chest -> {
            chest.startHopperTaskIfNeeded(this);
            String id = chest.getInfo().getId();
            Optional<CustomChestInfo> infoOptional = chestsLoader.getCustomChestInfoById(id);
            if (!infoOptional.isPresent()) {
                // Chest info removed from config, just going to keep it just in case
                return;
            }
            chest.setInfo(infoOptional.get());
        });
    }

    public void reloadAll() {
        reloadChests();
        reloadMessages();
        readConfig();
        redoChests();
    }

    private void registerListeners() {
        WorldChunkListener worldChunkListener = new WorldChunkListener(this);
        registerListeners(new ChestRecipeListener(this),
                new ChestPlaceListener(this, worldChunkListener),
                new ChestOpenCloseUseListener(this, worldChunkListener),
                worldChunkListener,
                new ChestDestroyListener(this, worldChunkListener),
                new ChestExplodeListener(this, worldChunkListener),
                new ChestHopperListener(this));
    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    private void disable(String message, Throwable throwable) {
        if (message != null) {
            if (throwable != null) {
                getLogger().log(Level.SEVERE, message, throwable);
            } else {
                getLogger().log(Level.SEVERE, message);
            }
        }
        Bukkit.getPluginManager().disablePlugin(this);
    }

    public ReflectionUtil getReflectionUtil() {
        return reflectionUtil;
    }

    public NBTUtil getNbtUtil() {
        return nbtUtil;
    }

    public ItemUtil getItemUtil() {
        return itemUtil;
    }

    public EffectUtil getEffectUtil() {
        return effectUtil;
    }

    public ChestsLoader getChestsLoader() {
        return chestsLoader;
    }

    public Map<Location, CustomChest> getChestMap() {
        return chestMap;
    }

    public Messages getMessages() {
        return messages;
    }

    public boolean isResetRecipes() {
        return resetRecipes;
    }

    public boolean isSupportHoppers() {
        return supportHoppers;
    }

    public int getHopperSpeed() {
        return hopperSpeed;
    }
}
