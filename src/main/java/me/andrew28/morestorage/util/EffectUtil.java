package me.andrew28.morestorage.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author xXAndrew28Xx
 */
public class EffectUtil {
    /* BlockPosition (NMS) */
    private Class<?> blockPositionClass;
    private Constructor blockPositionConstructor;
    /* Block (NMS) */
    private Class<?> blockClass;
    /* Blocks (NMS) */
    private Class<?> blocksClass;
    /* CraftWorld (OBC) */
    private Class<?> craftWorldClass;
    private Method getWorldHandleMethod;
    /* World (NMS) */
    private Class<?> worldClass;
    private Method getTileEntityMethod;
    private Method playBlockActionMethod;
    /* TileEntity (NMS) */
    private Class<?> tileEntityClass;
    private Method getBlockMethod;

    public EffectUtil(ReflectionUtil reflectionUtil) throws ReflectiveOperationException {
        Validate.notNull(reflectionUtil, "The ReflectionUtil cannot be null");
        /* BlockPosition (NMS) */
        this.blockPositionClass = reflectionUtil.getNMSClass("BlockPosition");
        this.blockPositionConstructor = blockPositionClass.getConstructor(int.class, int.class, int.class);
        /* Block (NMS) */
        this.blockClass = reflectionUtil.getNMSClass("Block");
        /* Blocks (NMS) */
        this.blocksClass = reflectionUtil.getNMSClass("Blocks");
        /* CraftWorld (OBC) */
        this.craftWorldClass = reflectionUtil.getOBCClass("CraftWorld");
        this.getWorldHandleMethod = craftWorldClass.getDeclaredMethod("getHandle");
        /* World (NMS) */
        this.worldClass = reflectionUtil.getNMSClass("World");
        this.getTileEntityMethod = worldClass.getMethod("getTileEntity", blockPositionClass);
        this.playBlockActionMethod = worldClass.getMethod("playBlockAction",
                blockPositionClass, blockClass, int.class, int.class);
        /* TileEntity (NMS) */
        this.tileEntityClass = reflectionUtil.getNMSClass("TileEntity");
        try {
            this.getBlockMethod = tileEntityClass.getDeclaredMethod("getBlock");
        } catch (NoSuchMethodException e) {
            this.getBlockMethod = tileEntityClass.getDeclaredMethod("w");
        }
    }

    private Object getBlockPosition(Block block) throws ReflectiveOperationException {
        return getBlockPosition(block.getX(), block.getY(), block.getZ());
    }

    private Object getBlockPosition(int x, int y, int z) throws ReflectiveOperationException {
        return blockPositionConstructor.newInstance(x, y, z);
    }

    public void changeChestOpenState(Block block, boolean open) throws ReflectiveOperationException {
        if (block == null ||
                (block.getType() != Material.CHEST
                        && block.getType() != Material.ENDER_CHEST
                        || block.getType() != Material.TRAPPED_CHEST)) {
            return;
        }
        Object blockPosition = getBlockPosition(block);

        World world = block.getWorld();
        Object nmsWorld = getWorldHandleMethod.invoke(world);

        Object tileEntity = getTileEntityMethod.invoke(nmsWorld, blockPosition);
        Object nmsBlock = getBlockMethod.invoke(tileEntity);
        playBlockActionMethod.invoke(nmsWorld, blockPosition, nmsBlock, 1, open ? 1 : 0);
    }
}
