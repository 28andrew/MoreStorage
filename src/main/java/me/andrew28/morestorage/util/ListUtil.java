package me.andrew28.morestorage.util;

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author xXAndrew28Xx
 */
public class ListUtil {
    public static <T> T[][] toArray(List<List<T>> list, Class<T> typeClass, Class<T[]> typeArrayClass) {
        T[][] array = (T[][]) Array.newInstance(typeArrayClass, list.size());
        for (int i = 0; i < list.size(); i++) {
            List<T> innerList = list.get(i);
            array[i] = innerList.toArray((T[]) Array.newInstance(typeClass, innerList.size()));
        }
        return array;
    }
}
