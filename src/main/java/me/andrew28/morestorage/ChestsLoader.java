package me.andrew28.morestorage;

import me.andrew28.morestorage.chest.CustomChestInfo;
import me.andrew28.morestorage.chest.ItemFilter;
import me.andrew28.morestorage.recipe.CustomRecipe;
import me.andrew28.morestorage.recipe.ShapedCustomRecipe;
import me.andrew28.morestorage.recipe.ShapelessCustomRecipe;
import me.andrew28.morestorage.util.EnumUtil;
import me.andrew28.morestorage.util.ItemMetaUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Loads {@link CustomChestInfo}s from a configuration file
 *
 * @author xXAndrew28Xx
 */
// TODO : Redo this semi-spaghetti
public class ChestsLoader {
    private static final String PATTERNS_SECTION = "patterns";
    private static final String CHESTS_SECTION = "chests";
    private static final String CHEST_NAMESPACE_PREFIX = "chest:";
    private File file;
    private MoreStorage moreStorage;
    private Logger logger;
    private Collection<CustomChestInfo> lastLoadedChests;

    ChestsLoader(File file, MoreStorage moreStorage) {
        Validate.notNull(file, "The file cannot be null");
        this.file = file;
        this.moreStorage = moreStorage;
        this.logger = moreStorage.getLogger();
    }

    public Collection<CustomChestInfo> load() {
        Configuration config = YamlConfiguration.loadConfiguration(file);

        /* Pattern Templates */
        boolean usingPatternTemplates = config.isConfigurationSection(PATTERNS_SECTION);
        Map<String, String[]> patternTemplates = new HashMap<>();
        if (usingPatternTemplates) {
            ConfigurationSection patternTemplatesSection = config.getConfigurationSection(PATTERNS_SECTION);
            for (String name : patternTemplatesSection.getKeys(false)) {
                if (!patternTemplatesSection.isList(name)) {
                    warn("Pattern template '" + name + "' is not a list");
                    continue;
                }
                List<String> patternTemplate = patternTemplatesSection.getStringList(name);
                patternTemplates.put(name, patternTemplate.toArray(new String[patternTemplate.size()]));
            }
        }

        /* Chests */
        if (!config.isConfigurationSection(CHESTS_SECTION)) {
            warn("No '" + CHESTS_SECTION + "' section found!");
            return Collections.emptyList();
        }
        ConfigurationSection chestsSection = config.getConfigurationSection(CHESTS_SECTION);
        List<CustomChestInfo> loadedChests = new ArrayList<>();
        for (String id : chestsSection.getKeys(false)) {
            if (!chestsSection.isConfigurationSection(id)) {
                warn("Chest '" + id + "' must be a section");
                continue;
            }
            ConfigurationSection chestSection = chestsSection.getConfigurationSection(id);

            /* Basic Info */
            String name = chestSection.contains("name") ? parseColor(chestSection.getString("name")) : id;
            String permission = chestSection.getString("permission");

            /* Item */
            ItemStack itemStack;
            try {
                itemStack = loadItem(chestSection, Material.CHEST, loadedChests);
            } catch (ItemLoadException e) {
                warn("The item for chest '" + id + "' is invalid: " + e.getMessage());
                continue;
            }

            /* Size */
            CustomChestInfo.SizeType sizeType;
            int size;
            if (chestSection.contains("slots")) {
                sizeType = CustomChestInfo.SizeType.SLOTS;
                size = chestSection.getInt("slots");
            } else {
                sizeType = CustomChestInfo.SizeType.ROWS;
                size = chestSection.getInt("rows", 3);
            }

            /* Recipes */
            List<CustomRecipe> customRecipes = new ArrayList<>();
            // User may specify a recipe section instead of a recipes section if they're only doing one recipe
            if (chestSection.contains("recipe")) {
                ConfigurationSection recipeSection = chestSection.getConfigurationSection("recipe");
                customRecipes.add(loadRecipe(patternTemplates, id, name, recipeSection, itemStack, loadedChests));
            } else if (chestSection.contains("recipes")) {
                ConfigurationSection recipesSection = chestSection.getConfigurationSection("recipes");
                for (String recipeName : recipesSection.getKeys(false)) {
                    ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeName);
                    customRecipes.add(loadRecipe(patternTemplates, id, recipeName, recipeSection, itemStack, loadedChests));
                }
            }
            if (moreStorage.isResetRecipes()) {
                moreStorage.getServer().clearRecipes();
            }
            customRecipes.forEach(CustomRecipe::register);

            CustomChestInfo chest = new CustomChestInfo(id, name, permission,
                    itemStack, sizeType, size, customRecipes);

            /* Item Filter */
            ItemFilter itemFilter = loadItemFilter(true, loadedChests, chestSection);
            if (itemFilter == null) {
                itemFilter = loadItemFilter(false, loadedChests, chestSection);
            }
            chest.setItemFilter(itemFilter);

            chest.setBlastProof(chestSection.getBoolean("blast-proof", false));
            chest.setRequireTopClearance(chestSection.getBoolean("require-top-clearance", true));
            chest.setAllowHoppers(chestSection.getBoolean("allow-hoppers", true));
            chest.setAllowDoubling(chestsSection.getBoolean("allow-doubling", true));

            loadedChests.add(chest);
        }

        lastLoadedChests = Collections.unmodifiableList(loadedChests);

        return lastLoadedChests;
    }

    private CustomRecipe loadRecipe(Map<String, String[]> recipeTemplates,
                                    String chestId, String name, ConfigurationSection section,
                                    ItemStack result, List<CustomChestInfo> loadedChests) {
        String recipeTypeName = section.getString("type", "SHAPED");
        CustomRecipe.CustomRecipeType recipeType = EnumUtil.get(CustomRecipe.CustomRecipeType.class, recipeTypeName);
        if (recipeType == null) {
            warn("Invalid recipe type in recipe '" + name + "' of chest '" + chestId + "'");
            return null;
        }
        String recipeKey = "chest:" + chestId + ":" + name;
        CustomRecipe recipe;
        if (recipeType.equals(CustomRecipe.CustomRecipeType.SHAPED)) {
            String[] pattern;
            if (section.isString("pattern")) {
                String templateName = section.getString("pattern");
                if (!recipeTemplates.containsKey(templateName)) {
                    warn("Invalid pattern template '" + templateName + "' in recipe '" + name +
                            "' of chest '" + chestId + "'");
                    return null;
                }
                pattern = recipeTemplates.get(templateName);
            } else if (section.isList("pattern")) {
                List<String> patternList = section.getStringList("pattern");
                if (patternList.size() > 3) {
                    warn("Pattern length must be at most 3 in recipe '" + name + "' of chest '" + chestId + "'");
                    return null;
                }
                pattern = patternList.toArray(new String[patternList.size()]);
            } else {
                warn("Missing (or existing by is invalid type) 'pattern'" +
                        " in recipe '" + name + "' of chest '" + chestId + "'");
                return null;
            }

            Map<Character, ItemStack> itemMap = new HashMap<>();
            for (String key : section.getKeys(false)) {
                if (key.length() != 1) {
                    continue;
                }
                boolean isString = section.isString(key);
                if (!isString && !section.isConfigurationSection(key)) {
                    continue;
                }
                char character = key.charAt(0);
                ItemStack itemStack;
                if (character == ' ') {
                    itemStack = null;
                } else if (isString) {
                    String id = section.getString(key);
                    Optional<ItemStack> itemStackOptional = findItemStackById(id, loadedChests);
                    if (!itemStackOptional.isPresent()) {
                        warn("Failed to find material for id '" + id +
                                "' in item '" + character + "' for recipe '" + name
                                + "' of chest '" + chestId + "'");
                        continue;
                    }
                    itemStack = itemStackOptional.get();
                } else {
                    try {
                        itemStack = loadItem(section, null, loadedChests);
                    } catch (ItemLoadException e) {
                        warn("Failed to load item '" + character
                                + "' for recipe '" + name +
                                "' of chest '" + chestId + "': " + e.getMessage());
                        continue;
                    }
                }
                itemMap.put(character, itemStack);
            }
            recipe = new ShapedCustomRecipe(result, moreStorage, recipeKey, itemMap, pattern);
        } else {
            List<ItemStack> items = new ArrayList<>();
            if (!section.contains("items")) {
                warn("Missing 'items' section in recipe '" + name + "' of chest '" + chestId + "'");
                return null;
            }
            ConfigurationSection itemsSection = section.getConfigurationSection("items");
            for (String key : itemsSection.getKeys(false)) {
                if (itemsSection.isString(key)) {
                    String id = itemsSection.getString(key);
                    Optional<ItemStack> itemStackOptional = findItemStackById(id, loadedChests);
                    if (!itemStackOptional.isPresent()) {
                        warn("Invalid id '" + id + " for an item in the shapeless recipe for the "
                                + chestId + " chest");
                        continue;
                    }
                    items.add(itemStackOptional.get());
                } else {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    try {
                        items.add(loadItem(itemSection, null, loadedChests));
                    } catch (ItemLoadException e) {
                        warn("Invalid item '" + key
                                + "' in recipe '" + name +
                                "' of chest '" + chestId + "': " + e.getMessage());
                    }
                }
            }
            recipe = new ShapelessCustomRecipe(result, moreStorage, recipeKey,
                    items.toArray(new ItemStack[items.size()]));
        }
        return recipe;
    }

    private ItemStack loadItem(ConfigurationSection section) throws ItemLoadException {
        return loadItem(section, null, null);
    }

    private ItemStack loadItem(ConfigurationSection section,
                               Material defaultMaterial, Collection<CustomChestInfo> loadedChests) throws ItemLoadException {
        Material material;
        findMaterial:
        {
            if (!section.contains("id") && !section.contains("type")) {
                if (defaultMaterial == null) {
                    throw new ItemLoadException("Missing 'id' or 'type'");
                } else {
                    material = defaultMaterial;
                    break findMaterial;
                }
            }

            String id = section.contains("id") ? section.getString("id") : section.getString("type");
            Optional<ItemStack> itemStackOptional = findItemStackById(id, loadedChests);
            if (itemStackOptional.isPresent()) {
                material = itemStackOptional.get().getType();
            } else {
                if (defaultMaterial == null) {
                    throw new ItemLoadException("Could not find material for id: " + id);
                } else {
                    material = defaultMaterial;
                }
            }
        }

        int amount = section.getInt("amount", 1);

        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (section.contains("name")) {
            itemMeta.setDisplayName(ChatColor.RESET + parseColor(section.getString("name")));
        }

        if (section.contains("lore")) {
            if (section.isString("lore")) {
                itemMeta.setLore(Collections.singletonList(parseColor(section.getString("lore"))));
            } else {
                itemMeta.setLore(section.getStringList("lore").stream()
                        .map(this::parseColor)
                        .collect(Collectors.toList()));
            }
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemFilter loadItemFilter(boolean whitelist, Collection<CustomChestInfo> loadedChests,
                                      ConfigurationSection chestSection) {
        ItemFilter itemFilter = null;
        String type = whitelist ? "whitelist" : "blacklist";
        String name = "item-" + type;
        if (chestSection.contains(name)) {
            if (chestSection.isString(name)) {
                String whitelistedItemId = chestSection.getString(name);
                Optional<ItemStack> itemStackOptional = findItemStackById(whitelistedItemId, loadedChests);
                if (!itemStackOptional.isPresent()) {
                    warn("The item id for a " + type + " item filter is invalid: " + whitelistedItemId);
                    return null;
                }
                itemFilter = new ItemFilter(true, Collections.singletonList(itemStackOptional.get().getType()));
            } else {
                List<Material> materials = new ArrayList<>();
                for (String whitelistedItemId : chestSection.getStringList(name)) {
                    Optional<ItemStack> itemStackOptional = findItemStackById(whitelistedItemId, loadedChests);
                    if (!itemStackOptional.isPresent()) {
                        warn("One of the item ids for a whitelist item filter is invalid: " + whitelistedItemId);
                        continue;
                    }
                    materials.add(itemStackOptional.get().getType());
                }
                itemFilter = new ItemFilter(true, materials);
            }
        }
        return itemFilter;
    }

    private Optional<ItemStack> findItemStackById(String id, Collection<CustomChestInfo> loadedChests) {
        ItemStack itemStack = null;
        if (loadedChests != null && id.startsWith(CHEST_NAMESPACE_PREFIX)) {
            final String chestId = id.substring(CHEST_NAMESPACE_PREFIX.length());
            Optional<CustomChestInfo> chest = loadedChests
                    .stream()
                    .filter(customChest -> customChest.getId().equals(chestId))
                    .findFirst();
            if (chest.isPresent()) {
                itemStack = chest.get().getItemStack();
            }
        } else {
            try {
                Material material = moreStorage.getItemUtil().getMaterialByMinecraftId(id);
                itemStack = material != null ? new ItemStack(material) : null;
            } catch (ReflectiveOperationException e) {
            }
        }
        return Optional.ofNullable(itemStack);
    }

    private void warn(String warning) {
        logger.warning(warning);
    }

    public Collection<CustomChestInfo> getLastLoadedChests() {
        return lastLoadedChests;
    }

    public Optional<CustomChestInfo> getCustomChestInfo(ItemStack itemStack) {
        if (lastLoadedChests == null) {
            return Optional.empty();
        }
        return lastLoadedChests
                .stream()
                .filter(chest -> ItemMetaUtil.checkBasicMetaMatches(chest.getItemStack(), itemStack))
                .findFirst();
    }

    public Optional<CustomChestInfo> getCustomChestInfoById(String id) {
        if (lastLoadedChests == null) {
            return Optional.empty();
        }
        return lastLoadedChests
                .stream()
                .filter(chest -> chest.getId().equals(id))
                .findFirst();
    }

    private String parseColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public class ItemLoadException extends Exception {
        private static final long serialVersionUID = 7838255621654277268L;

        ItemLoadException(String message) {
            super(message);
        }

        ItemLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
