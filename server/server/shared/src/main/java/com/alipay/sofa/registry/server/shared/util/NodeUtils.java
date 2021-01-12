package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class NodeUtils {

    public static <T extends Node> List<String> transferNodeToIpList(Collection<T> nodes) {
        List<String> result = new ArrayList<>(nodes.size());
        nodes.forEach(node -> result.add(node.getNodeUrl().getIpAddress()));
        return result;
    }
}
