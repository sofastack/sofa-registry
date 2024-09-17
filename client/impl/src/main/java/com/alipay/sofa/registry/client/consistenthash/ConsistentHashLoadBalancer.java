package com.alipay.sofa.registry.client.consistenthash;


import com.alipay.sofa.registry.client.remoting.ServerNode;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    private HashStrategy hashStrategy = new FnvHashStrategy();

    private int virtualNodeSize = DEFAULT_VIRTUAL_NODE_SIZE;
    private final static String VIRTUAL_NODE_SUFFIX = "&&";
    private final static int DEFAULT_VIRTUAL_NODE_SIZE=10;

    public ConsistentHashLoadBalancer() {
    }

    public ConsistentHashLoadBalancer(int virtualNodeSize) {
        this.virtualNodeSize = virtualNodeSize;
    }

    public ConsistentHashLoadBalancer(HashStrategy hashStrategy) {
        this.hashStrategy = hashStrategy;
    }

    public ConsistentHashLoadBalancer(int virtualNodeSize, HashStrategy hashStrategy) {
        this.virtualNodeSize = virtualNodeSize;
        this.hashStrategy = hashStrategy;
    }

    @Override
    public ServerNode select(List<ServerNode> servers, Invocation invocation) {
        int invocationHashCode = hashStrategy.getHashCode(invocation.getHashKey());
        TreeMap<Integer, ServerNode> ring = buildConsistentHashRing(servers);
        ServerNode server = locate(ring, invocationHashCode);
        return server;
    }

    private ServerNode locate(TreeMap<Integer, ServerNode> ring, int invocationHashCode) {
        Map.Entry<Integer, ServerNode> locateEntry = ring.ceilingEntry(invocationHashCode);
        if (locateEntry == null) {
            locateEntry = ring.firstEntry();
        }
        return locateEntry.getValue();
    }

    private TreeMap<Integer, ServerNode> buildConsistentHashRing(List<ServerNode> servers) {
        TreeMap<Integer, ServerNode> virtualNodeRing = new TreeMap<>();
        for (ServerNode server : servers) {
            for (int i = 0; i < virtualNodeSize; i++) {
                virtualNodeRing.put(hashStrategy.getHashCode(
                        server.getUrl() + VIRTUAL_NODE_SUFFIX + i), server);
            }
        }
        return virtualNodeRing;
    }
}
