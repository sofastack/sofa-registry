package com.alipay.sofa.registry.client.consistenthash;


import com.alipay.sofa.registry.client.remoting.ServerNode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public class KetamaConsistentHashLoadBalancer implements LoadBalancer {
    private static MessageDigest md5Digest;

    // 每一个物理节点的虚拟节点副本个数
    private int virtualNodeSize;

    private final static String VIRTUAL_NODE_SUFFIX = "-";

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    public KetamaConsistentHashLoadBalancer() {
    }

    public KetamaConsistentHashLoadBalancer(int virtualNodeSize) {
        this.virtualNodeSize = virtualNodeSize;
    }

    @Override
    public ServerNode select(List<ServerNode> servers, Invocation invocation) {
        long invocationHashCode = getHashCode(invocation.getHashKey());
        TreeMap<Long, ServerNode> ring = buildConsistentHashRing(servers);
        ServerNode server = locate(ring, invocationHashCode);
        return server;
    }

    private ServerNode locate(TreeMap<Long, ServerNode> ring, Long invocationHashCode) {
        Map.Entry<Long, ServerNode> locateEntry = ring.ceilingEntry(invocationHashCode);
        if (locateEntry == null) {
            locateEntry = ring.firstEntry();
        }
        return locateEntry.getValue();
    }

    private TreeMap<Long, ServerNode> buildConsistentHashRing(List<ServerNode> servers) {
        TreeMap<Long, ServerNode> virtualNodeRing = new TreeMap<>();
        for (ServerNode server : servers) {
            for (int i = 0; i < virtualNodeSize / 4; i++) {
                byte[] digest = computeMd5(server.getUrl() + VIRTUAL_NODE_SUFFIX + i);
                for (int h = 0; h < 4; h++) {
                    Long k = ((long) (digest[3 + h * 4] & 0xFF) << 24)
                            | ((long) (digest[2 + h * 4] & 0xFF) << 16)
                            | ((long) (digest[1 + h * 4] & 0xFF) << 8)
                            | (digest[h * 4] & 0xFF);
                    virtualNodeRing.put(k, server);

                }
            }
        }
        return virtualNodeRing;
    }

    private long getHashCode(String origin) {
        byte[] bKey = computeMd5(origin);
        long rv = ((long) (bKey[3] & 0xFF) << 24)
                | ((long) (bKey[2] & 0xFF) << 16)
                | ((long) (bKey[1] & 0xFF) << 8)
                | (bKey[0] & 0xFF);
        return rv;
    }

    private static byte[] computeMd5(String k) {
        MessageDigest md5;
        try {
            md5 = (MessageDigest) md5Digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone of MD5 not supported", e);
        }
        md5.update(k.getBytes());
        return md5.digest();
    }
}
