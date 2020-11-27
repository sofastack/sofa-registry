package com.alipay.sofa.registry.server.meta.slot.impl;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.Observer;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeAdded;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeModified;
import com.alipay.sofa.registry.server.meta.cluster.node.NodeRemoved;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.slot.ArrangeTaskDispatcher;
import com.alipay.sofa.registry.server.meta.slot.SlotArranger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author chen.zhu
 * <p>
 * Nov 24, 2020
 */
@Component
public class DefaultSlotArranger implements SlotArranger, Observer {

    @Autowired
    private DataServerManager dataServerManager;

    @Autowired
    private ArrangeTaskDispatcher<DataNode> arrangeTaskDispatcher;

    @PostConstruct
    public void postConstruct() {
        dataServerManager.addObserver(this);
    }

    @PreDestroy
    public void preDestroy() {
        dataServerManager.removeObserver(this);
    }

    @Override
    public void update(Observable source, Object message) {
        if(message instanceof NodeAdded) {
            onServerAdded(((NodeAdded<DataNode>) message).getNode());
        }
        if(message instanceof NodeRemoved) {
            onServerRemoved(((NodeRemoved<DataNode>) message).getNode());
        }
        if(message instanceof NodeModified) {
            NodeModified<DataNode> nodeModified = (NodeModified<DataNode>) message;
            onServerChanged(nodeModified.getOldNode(), nodeModified.getNewNode());
        }
    }

    @Override
    public void onServerAdded(DataNode newNode) {
        arrangeTaskDispatcher.serverAlive(newNode);
    }

    @Override
    public void onServerRemoved(DataNode node) {
        arrangeTaskDispatcher.serverDead(node);
    }

    @Override
    public void onServerChanged(DataNode oldNode, DataNode newNode) {

    }
}
