package me.andrew28.morestorage.chest;

import me.andrew28.morestorage.recipe.CustomRecipe;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents the info which may be used for multiple {@link CustomChest}s
 *
 * @author xXAndrew28Xx
 * @since 0.1.0
 */
public class CustomChestInfo {
    private static final int SLOTS_IN_A_ROW = 9;
    private String id, name, permission;
    private ItemStack itemStack;
    private SizeType sizeType;
    private int size;
    private List<CustomRecipe> recipes;
    private boolean blastProof = false;
    private ItemFilter itemFilter;
    private boolean requireTopClearance, allowHoppers, allowDoubling = true;

    /**
     * Creates a new {@link CustomChestInfo}
     *
     * @param id         The unique id
     * @param name       The display name
     * @param permission The permission needed (may be null)
     * @param itemStack  The {@link ItemStack}
     * @param sizeType   The {@link SizeType} of the size parameter
     * @param size       The size represented in the sizeType parameter
     * @param recipes    The list of crafting recipes
     */
    public CustomChestInfo(String id, String name, String permission, ItemStack itemStack, SizeType sizeType,
                           int size, List<CustomRecipe> recipes) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.itemStack = itemStack;
        this.sizeType = sizeType;
        this.size = size;
        this.recipes = recipes;
    }

    /**
     * Gets the unique id
     *
     * @return The unique id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id
     *
     * @param id The unique id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the display name
     *
     * @return The display name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name
     *
     * @param name The display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the permission. If null, there is no permission check.
     *
     * @return The permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Sets the permission. If null is given, the permission check will be disabled.
     *
     * @param permission The permission
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Gets the ItemStack which may be given to players
     *
     * @return The ItemStack
     */
    public ItemStack getItemStack() {
        if (itemStack == null) {
            return null;
        }
        return itemStack.clone();
    }

    /**
     * Sets the ItemStack, which players will use to place chests with this info
     *
     * @param itemStack The ItemStack
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Get the size type that {@link #getSize()} returns in
     *
     * @return The size type that {@link #getSize()} returns in
     */
    public SizeType getSizeType() {
        return sizeType;
    }

    /**
     * Sets the {@link SizeType} that {@link #getSize()} should return in.
     * It is recommended to use {@link #setSize(int, SizeType)} instead.
     *
     * @param sizeType The size type to set to
     */
    public void setSizeType(SizeType sizeType) {
        this.sizeType = sizeType;
    }

    /**
     * Set both the size and {@link SizeType}
     *
     * @param size     The size to set to
     * @param sizeType The {@link SizeType} to set to
     */
    public void setSize(int size, SizeType sizeType) {
        this.size = size;
        this.sizeType = sizeType;
    }

    /**
     * Gets the required amount of rows a {@link Inventory} must have to store the maximum amount of items, without
     * running out space.
     *
     * @return The required amount of rows
     */
    public int getRequiredRows() {
        return sizeType == SizeType.SLOTS ? (int) Math.ceil(size / (double) 9) : size;
    }

    /**
     * Gets the size in type {@link SizeType#SLOTS}
     *
     * @return The size in slots
     */
    public int getSlots() {
        return sizeType == SizeType.SLOTS ? size : size * SLOTS_IN_A_ROW;
    }

    /**
     * Gets the size in the type specified by {@link #getSizeType()}
     *
     * @return The size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the size. It is recommended to use {@link #setSize(int, SizeType)} instead.
     *
     * @param size The size to set to
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Gets the recipes that may be used to craft chests with this info
     *
     * @return The recipes that may be used to crafts with this info
     */
    public List<CustomRecipe> getRecipes() {
        return recipes;
    }

    /**
     * Gets whether chests with this info are immune from explosions
     *
     * @return Whether chests with this info are immune from explosions
     */
    public boolean isBlastProof() {
        return blastProof;
    }

    /**
     * Set whether chests with this info are immune from explosions
     *
     * @param blastProof Whether chests with this info should be immune from explosions
     */
    public void setBlastProof(boolean blastProof) {
        this.blastProof = blastProof;
    }

    /**
     * Gets the item filter used when items are added/removed from chests with this info's inventories,
     * which may be null.
     *
     * @return The item filter to be used, if null then no filter is used
     */
    public ItemFilter getItemFilter() {
        return itemFilter;
    }

    /**
     * Sets the item filter to use when items are added/removed from chests with this info's inventories.
     *
     * @param itemFilter The item filter to be used, if null then no filter will be used
     */
    public void setItemFilter(ItemFilter itemFilter) {
        this.itemFilter = itemFilter;
    }

    /**
     * Gets whether chests with this info require that the block above ({@link org.bukkit.block.BlockFace#UP},
     * needs to be not occluding (!{@link Material#isOccluding()})
     *
     * @return Whether chests with this info need above clearance
     */
    public boolean isRequireTopClearance() {
        return requireTopClearance;
    }

    /**
     * Sets whether chests with this info require that the block above ({@link org.bukkit.block.BlockFace#UP},
     * needs to be not occluding (!{@link Material#isOccluding()})
     *
     * @param requireTopClearance Whether chests with this info need above clearance
     */
    public void setRequireTopClearance(boolean requireTopClearance) {
        this.requireTopClearance = requireTopClearance;
    }

    /**
     * Gets whether hoppers should function on chests with this info
     *
     * @return Whether hoppers should function on chests with this info
     */
    public boolean isAllowHoppers() {
        return allowHoppers;
    }

    /**
     * Sets whether hoppers should function on chests with this info
     *
     * @param allowHoppers Whether hoppers should function on chests with this info
     */
    public void setAllowHoppers(boolean allowHoppers) {
        this.allowHoppers = allowHoppers;
    }

    /**
     * Gets whether chests with this info are allowed to be placed next to chests with the
     * same {@link Material} type, which makes them double with adjacent chests.
     *
     * @return Whether chests with this info are allowed to become a double chest
     */
    public boolean isAllowDoubling() {
        return allowDoubling;
    }

    /**
     * Sets whether chests with this info are allowed to be placed next to chests with the
     * same {@link Material} type, which makes them double with adjacent chests.
     *
     * @param allowDoubling Whether chests with this info are allowed to become a double chest
     */
    public void setAllowDoubling(boolean allowDoubling) {
        this.allowDoubling = allowDoubling;
    }

    /**
     * Creates an inventory (constrained to this info) for a custom chest
     *
     * @param customChest The CustomChest to create an inventory for
     * @return The generated Inventory (with holder {@link CustomChestInventoryHolder})
     */
    public Inventory createInventory(CustomChest customChest) {
        CustomChestInventoryHolder inventoryHolder = new CustomChestInventoryHolder(customChest);
        Inventory inventory = Bukkit.createInventory(inventoryHolder, getRequiredRows() * 9, name);
        inventoryHolder.setInventory(inventory);
        return inventory;
    }

    /**
     * Creates a custom chest for a Block, with this info
     *
     * @param block The block to create a custom chest for
     * @return The generated custom chest
     */
    public CustomChest createChest(Block block) {
        CustomChest customChest = new CustomChest(this, block, null);
        customChest.setInventory(createInventory(customChest));
        return customChest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof CustomChestInfo)) return false;

        CustomChestInfo chest = (CustomChestInfo) o;

        return new EqualsBuilder()
                .append(size, chest.size)
                .append(blastProof, chest.blastProof)
                .append(id, chest.id)
                .append(name, chest.name)
                .append(permission, chest.permission)
                .append(sizeType, chest.sizeType)
                .append(recipes, chest.recipes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(permission)
                .append(sizeType)
                .append(size)
                .append(recipes)
                .append(blastProof)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("permission", permission)
                .append("sizeType", sizeType)
                .append("size", size)
                .append("recipes", recipes)
                .append("blastProof", blastProof)
                .toString();
    }

    public enum SizeType {
        SLOTS, ROWS
    }
}
