package com.alipay.sofa.registry.util;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author chen.zhu
 * <p>
 * Nov 23, 2020
 */
public class MapUtils {

    public static  <K, V>  V getOrCreate(ConcurrentMap<K, V> map, K key, ObjectFactory<V>  objectFactory){

        V value = map.get(key);
        if(value != null){
            return value;
        }

        value = map.get(key);
        if(value == null){
            value = objectFactory.create();
            map.put(key, value);
        }
        return value;
    }
}
