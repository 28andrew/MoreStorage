package me.andrew28.morestorage.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author xXAndrew28Xx
 */
public class NBTUtil {
    private ReflectionUtil reflectionUtil;
    /* NBTTagCompound (NMS) */
    private Class<?> nbtTagCompoundClass;
    private Constructor<?> nbtTagCompoundConstructor;
    /* NBTCompressedTools */
    private Class<?> nbtCompressedStreamToolsClass;
    private Method saveNBTToStreamMethod, readNBTFromStreamMethod;

    public NBTUtil(ReflectionUtil reflectionUtil) throws ReflectiveOperationException {
        this.reflectionUtil = reflectionUtil;

        /* NBTTagCompound (NMS) */
        this.nbtTagCompoundClass = reflectionUtil.getNMSClass("NBTTagCompound");
        this.nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
        /* NBTCompressedStreamTools (NMS) */
        this.nbtCompressedStreamToolsClass = reflectionUtil.getNMSClass("NBTCompressedStreamTools");
        this.saveNBTToStreamMethod = nbtCompressedStreamToolsClass
                .getMethod("a", nbtTagCompoundClass, OutputStream.class);
        this.readNBTFromStreamMethod = nbtCompressedStreamToolsClass
                .getMethod("a", InputStream.class);
    }

    public Object getNewNbtTagCompound() throws ReflectiveOperationException {
        return nbtTagCompoundConstructor.newInstance();
    }

    public void writeNBTTagCompoundToStream(Object nbtTagCompound, OutputStream outputStream) throws InvocationTargetException, IllegalAccessException {
        saveNBTToStreamMethod.invoke(null, nbtTagCompound, outputStream);
    }

    public Object readNBTTagCompoundFromStream(InputStream inputStream) throws ReflectiveOperationException {
        return readNBTFromStreamMethod.invoke(null, inputStream);
    }

    /* Getters */

    public ReflectionUtil getReflectionUtil() {
        return reflectionUtil;
    }

    public Class<?> getNbtTagCompoundClass() {
        return nbtTagCompoundClass;
    }

    public Constructor<?> getNbtTagCompoundConstructor() {
        return nbtTagCompoundConstructor;
    }

    public Class<?> getNbtCompressedStreamToolsClass() {
        return nbtCompressedStreamToolsClass;
    }

    public Method getSaveNBTToStreamMethod() {
        return saveNBTToStreamMethod;
    }

    public Method getReadNBTFromStreamMethod() {
        return readNBTFromStreamMethod;
    }
}
