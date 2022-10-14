package com.alipay.sofa.registry.client.consistenthash;

import com.alipay.sofa.registry.client.remoting.ServerNode;

import java.util.List;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public interface LoadBalancer {

    ServerNode select(List<ServerNode> servers, Invocation invocation);
}
