package com.github.seemethere.DeathEssentials.utils.commonutils;

import java.util.*;

public class ModuleUtil {
    public static <K extends Comparable, V extends Comparable>Map<K, V> sortByValues(Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        Map<K, V> sortedMap = new TreeMap<K, V>(Collections.reverseOrder());
        for (Map.Entry<K,V> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    //TODO: Create a pagination method
}
