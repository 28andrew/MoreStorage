package me.andrew28.morestorage.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author xXAndrew28Xx
 */
public class ReflectionUtil {
    private static final String NMS_PACKAGE_PREFIX_FORMAT = "net.minecraft.server.%s";
    private static final String OBC_PACKAGE_PREFIX_FORMAT = "org.bukkit.craftbukkit.%s";
    private static final String PACKAGE_SEPERATOR = ".";

    private String serverVersion;
    private String nmsPackagePrefix;
    private String obcPackagePrefix;

    public ReflectionUtil(String serverVersion) {
        this.serverVersion = serverVersion;
        this.nmsPackagePrefix = String.format(NMS_PACKAGE_PREFIX_FORMAT, serverVersion);
        this.obcPackagePrefix = String.format(OBC_PACKAGE_PREFIX_FORMAT, serverVersion);
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public Class<?> getNMSClass(String packageSuffix) throws ReflectiveOperationException {
        return Class.forName(nmsPackagePrefix + PACKAGE_SEPERATOR + packageSuffix);
    }

    public Class<?> getOBCClass(String packageSuffix) throws ClassNotFoundException {
        return Class.forName(obcPackagePrefix + PACKAGE_SEPERATOR + packageSuffix);
    }

    public Method getMethod(Class<?> clazz, String name, Class<?>[] parameterTypes, MethodOption... methodOptions) throws NoSuchMethodException {
        Method method = clazz.getMethod(name, parameterTypes);
        for (MethodOption methodOption : methodOptions) {
            switch (methodOption) {
                case MAKE_ACCESSIBLE:
                    method.setAccessible(true);
                    break;
            }
        }
        return method;
    }

    public Field getField(Class<?> clazz, String name, FieldOption... fieldOptions) throws ReflectiveOperationException {
        Field field = clazz.getField(name);
        for (FieldOption fieldOption : fieldOptions) {
            switch (fieldOption) {
                case MAKE_ACCESSIBLE:
                    field.setAccessible(true);
                    break;
                case MAKE_NON_FINAL:
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                    break;
            }
        }
        return field;
    }

    public void setFieldValue(Object object, String name, Object value) throws ReflectiveOperationException {
        Field field = getField(object.getClass(), name, FieldOption.MAKE_ACCESSIBLE, FieldOption.MAKE_NON_FINAL);
        field.set(object, value);
    }

    public enum MethodOption {
        MAKE_ACCESSIBLE
    }

    public enum FieldOption {
        MAKE_ACCESSIBLE, MAKE_NON_FINAL
    }
}
