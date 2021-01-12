package com.alipay.sofa.registry.comparator;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class Pair<K, V> {

    private K key;

    private V val;

    public Pair() {
    }

    public Pair(K key, V val) {
        this.key = key;
        this.val = val;
    }

    public static <K, V> Pair<K, V> from(K k, V v) {
        return new Pair<K, V>(k, v);
    }

    public K getKey() {
        return key;
    }

    public Pair<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    public V getVal() {
        return val;
    }

    public Pair<K, V> setVal(V val) {
        this.val = val;
        return this;
    }
}
