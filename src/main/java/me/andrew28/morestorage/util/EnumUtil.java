package me.andrew28.morestorage.util;

/**
 * @author xXAndrew28Xx
 */
public class EnumUtil {
    public static <E extends Enum<E>> E get(Class<E> e, String id) {
        try {
            return Enum.valueOf(e, id);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
