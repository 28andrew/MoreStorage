package me.andrew28.morestorage.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author xXAndrew28Xx
 */
public class ItemUtil {
    private NBTUtil nbtUtil;
    /* ItemStack (CB) */
    private Class<?> itemStackClass;
    private Constructor<?> itemStackNBTConstructor;
    private Method createStackMethod, stackSaveMethod, getItemMethod;
    /* Item (NMS) */
    private Class<?> itemClass;
    private Object registry;
    private Field registryField;
    /* CraftItemStack (CB) */
    private Class<?> craftItemStackClass;
    private Method asNMSCopyMethod, asBukkitCopyMethod, fromItemMethod;
    private Field itemStackField;
    /* MinecraftKey (NMS) */
    private Class<?> minecraftKeyClass;
    private Constructor<?> minecraftKeyConstructor;
    /* RegistryMaterials (NMS) */
    private Class<?> registryMaterialsClass;
    private Method mcIdFromNMSStackMethod;
    private Method itemFromMcIdMethod;

    public ItemUtil(NBTUtil nbtUtil) throws ReflectiveOperationException {
        Validate.notNull(nbtUtil, "The NBTUtil cannot be null.");
        this.nbtUtil = nbtUtil;
        ReflectionUtil reflectionUtil = nbtUtil.getReflectionUtil();

        Class<?> nbtTagCompoundClass = nbtUtil.getNbtTagCompoundClass();

        /* ItemStack (NMS) */
        this.itemStackClass = reflectionUtil.getNMSClass("ItemStack");
        try {
            this.itemStackNBTConstructor = itemStackClass.getConstructor(nbtTagCompoundClass);
        } catch (NoSuchMethodException e) {
            this.createStackMethod = itemStackClass.getMethod("createStack", nbtTagCompoundClass);
        }
        this.getItemMethod = itemStackClass.getMethod("getItem");
        this.stackSaveMethod = itemStackClass.getMethod("save", nbtTagCompoundClass);
        /* Item (NMS) */
        this.itemClass = reflectionUtil.getNMSClass("Item");
        this.registryField = itemClass.getDeclaredField("REGISTRY");
        /* CraftItemStack (CB) */
        this.craftItemStackClass = reflectionUtil.getOBCClass("inventory.CraftItemStack");
        this.asNMSCopyMethod = craftItemStackClass.getMethod("asNMSCopy", ItemStack.class);
        this.asBukkitCopyMethod = craftItemStackClass.getMethod("asBukkitCopy", itemStackClass);
        this.fromItemMethod = craftItemStackClass.getMethod("asNewCraftStack", itemClass);
        this.itemStackField = craftItemStackClass.getDeclaredField("handle");
        this.itemStackField.setAccessible(true);
        /* MinecraftKey (NMS) */
        minecraftKeyClass = reflectionUtil.getNMSClass("MinecraftKey");
        minecraftKeyConstructor = minecraftKeyClass.getConstructor(String.class);
        /* RegistryMaterials (NMS) */
        this.registryMaterialsClass = reflectionUtil.getNMSClass("RegistryMaterials");
        this.mcIdFromNMSStackMethod = registryMaterialsClass.getMethod("b", Object.class);
        this.itemFromMcIdMethod = registryMaterialsClass.getMethod("get", Object.class);
    }

    public Object getNMSItemStack(ItemStack itemStack) throws ReflectiveOperationException {
        return asNMSCopyMethod.invoke(null, itemStack);
    }

    public Object getItemFromNMSStack(Object nmsStack) throws ReflectiveOperationException {
        return getItemMethod.invoke(nmsStack);
    }

    public Object getCraftItemStackFromItem(Object item) throws ReflectiveOperationException {
        return fromItemMethod.invoke(null, item);
    }

    public Object getItemRegistry() throws IllegalAccessException {
        if (registry == null) {
            registry = registryField.get(null);
        }
        return registry;
    }

    public String getMinecraftId(Material material) throws ReflectiveOperationException {
        Object nmsItemStack = getNMSItemStack(new ItemStack(material));
        Object item = getItemFromNMSStack(nmsItemStack);
        Object registry = getItemRegistry();
        Object minecraftKey = mcIdFromNMSStackMethod.invoke(registry, item);
        return minecraftKey.toString();
    }

    public Material getMaterialByMinecraftId(String id) throws ReflectiveOperationException {
        Object registry = getItemRegistry();
        Object mcKey = minecraftKeyConstructor.newInstance(id);
        Object item = itemFromMcIdMethod.invoke(registry, mcKey);
        if (item == null) {
            return null;
        }
        Object nmsItemStack = getNMSItemStackFromCraftItemStack(getCraftItemStackFromItem(item));
        return getItemStackFromNMSItemStack(nmsItemStack).getType();
    }

    public Object saveItemStackToNBT(ItemStack itemStack) throws ReflectiveOperationException {
        Object nbtTagCompound = nbtUtil.getNewNbtTagCompound();
        saveItemStackToNBT(itemStack, nbtTagCompound);
        return nbtTagCompound;
    }

    public void saveItemStackToNBT(ItemStack itemStack, Object nbtTagCompound) throws ReflectiveOperationException {
        if (nbtTagCompound == null) {
            nbtTagCompound = nbtUtil.getNewNbtTagCompound();
        }
        Object nmsItemStack = getNMSItemStack(itemStack);
        stackSaveMethod.invoke(nmsItemStack, nbtTagCompound);
    }

    public void serializeItemStack(ItemStack itemStack, OutputStream outputStream) throws ReflectiveOperationException {
        Validate.notNull(itemStack, "The ItemStack cannot be null.");

        Object nbtTagCompound = saveItemStackToNBT(itemStack);
        nbtUtil.writeNBTTagCompoundToStream(nbtTagCompound, outputStream);
    }

    public byte[] serializeItemStack(ItemStack itemStack) throws ReflectiveOperationException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializeItemStack(itemStack, outputStream);
        return outputStream.toByteArray();
    }

    public ItemStack getItemStackFromNMSItemStack(Object nmsItemStack) throws ReflectiveOperationException {
        return (ItemStack) asBukkitCopyMethod.invoke(null, nmsItemStack);
    }

    public Object getNMSItemStackFromCraftItemStack(Object craftItemStack) throws IllegalAccessException {
        return itemStackField.get(craftItemStack);
    }

    public Object getNMSItemStackFromNBTTagCompound(Object nbtTagCompound) throws ReflectiveOperationException {
        if (itemStackNBTConstructor != null) {
            return itemStackNBTConstructor.newInstance(nbtTagCompound);
        }
        return createStackMethod.invoke(null, nbtTagCompound);
    }

    public ItemStack getItemStackFromNBTTagCompound(Object nbtTagCompound) throws ReflectiveOperationException {
        return getItemStackFromNMSItemStack(getNMSItemStackFromNBTTagCompound(nbtTagCompound));
    }

    public ItemStack deserializeItemStack(InputStream inputStream) throws ReflectiveOperationException {
        Object nbtTagCompound = nbtUtil.readNBTTagCompoundFromStream(inputStream);
        return getItemStackFromNBTTagCompound(nbtTagCompound);
    }

    public ItemStack deserializeItemStack(byte[] bytes) throws ReflectiveOperationException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        return deserializeItemStack(inputStream);
    }

    /* Getters */

    public NBTUtil getNbtUtil() {
        return nbtUtil;
    }

    public Class<?> getItemStackClass() {
        return itemStackClass;
    }

    public Constructor<?> getItemStackNBTConstructor() {
        return itemStackNBTConstructor;
    }

    public Method getCreateStackMethod() {
        return createStackMethod;
    }

    public Method getStackSaveMethod() {
        return stackSaveMethod;
    }

    public Class<?> getCraftItemStackClass() {
        return craftItemStackClass;
    }

    public Method getAsNMSCopyMethod() {
        return asNMSCopyMethod;
    }

    public Method getAsBukkitCopyMethod() {
        return asBukkitCopyMethod;
    }
}
