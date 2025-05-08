package com.magicbox.cloud.terminal.note;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实现LRU缓存
 * 根据最近使用的原则，保留最近被访问过的数据项在缓存中
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    // 构造方法
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // accessOrder 为 true 表示按照访问顺序排序 最近访问的在最后
        this.capacity = capacity;
    }

    // 重写 removeEldestEntry 方法，当元素超过容量时移除最旧的元素
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    public static void main(String[] args) {
        LRUCache<Integer, String> lruCache = new LRUCache<>(3);

        lruCache.put(1, "A");
        lruCache.put(2, "B");
        lruCache.put(3, "C");
        System.out.println("Initial Cache: " + lruCache);

        lruCache.get(2); // 访问键 2
        System.out.println("After accessing 2: " + lruCache);

        lruCache.put(4, "D"); // 插入新元素
        System.out.println("After adding 4: " + lruCache);

        lruCache.get(1); // 访问键 1（不存在）
        lruCache.put(5, "E"); // 再插入一个新元素
        System.out.println("After adding 5: " + lruCache);

        /**
         * 初始状态：{1=A, 2=B, 3=C}
         * 访问键 2 后，键 2 被认为最近使用，顺序变为：{1=A, 3=C, 2=B}
         * 插入键 4，超出容量，移除最近最少使用的键 1：{3=C, 2=B, 4=D}
         * 插入键 5，再次超出容量，移除键 3：{2=B, 4=D, 5=E}
         */
    }
}
