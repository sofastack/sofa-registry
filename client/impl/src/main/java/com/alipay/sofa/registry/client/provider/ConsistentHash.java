package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.remoting.ServerNode;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author liqiuliang
 * @create 2022-09-28 11:54
 */
public class ConsistentHash {
    /**
     * List of real clusters
     */
    private static List<String> realGroups = new LinkedList<>();

    /**
     * Virtual Node Mapping Relationship
     */
    private static SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    private static final int VIRTUAL_NODE_NUM = 10;

    public ConsistentHash(List<ServerNode> allServers) {
        for (ServerNode allServer : allServers) {
            realGroups.add(allServer.getUrl());
        }
        init();
    }

    public void init() {
        // Map virtual nodes to Hash rings
        for (String realGroup : realGroups) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNodeName = getVirtualNodeName(realGroup, i);
                int hash = getHash(virtualNodeName);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }

    public static String getVirtualNodeName(String realName, int num) {
        return realName + "&&VN" + num;
    }

    public static String getRealNodeName(String virtualName) {
        return virtualName.split("&&")[0];
    }

    public static String choose(String key) {
        int hash = getHash(key);
        // Only take out all parts larger than the hash value without traversing the entire tree
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNodeName;
        if (subMap == null || subMap.isEmpty()) {
            // The hash value is at the end and should be mapped to the first group
            virtualNodeName = virtualNodes.get(virtualNodes.firstKey());
        } else {
            virtualNodeName = subMap.get(subMap.firstKey());
        }
        return getRealNodeName(virtualNodeName);
    }

    public static void refreshHashCircle() {
        // When the cluster changes, refresh the hash ring,
        // and the position of the rest of the clusters on the hash ring will not change
        virtualNodes.clear();
        for (String realGroup : realGroups) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                String virtualNodeName = getVirtualNodeName(realGroup, i);
                int hash = getHash(virtualNodeName);
                System.out.println("[" + virtualNodeName + "] launched @ " + hash);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }

    public static void addGroup(String identifier) {
        realGroups.add(identifier);
        refreshHashCircle();
    }

    public static void removeGroup(String identifier) {
        int i = 0;
        for (String group : realGroups) {
            if (group.equals(identifier)) {
                realGroups.remove(i);
            }
            i++;
        }
        refreshHashCircle();
    }

    /**
     * Calculate the Hash value, using the FNV1_32_HASH algorithm
     *
     * @param str
     * @return
     */
    public static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }
}
