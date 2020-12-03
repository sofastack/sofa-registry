package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 * <p>
 * Provide Data is designed for two scenerio, as below:
 * 1. Dynamic Configs inside Sofa-Registry itself
 * 2. Service Gaven (or say 'Watcher') are subscring messages through Session-Server
 * <p>
 * All above user cases stages a Config Center role by Sofa-Registry
 * And all these infos are madantorily persistenced to disk
 * So, by leveraging meta server's JRaft feature, infos are reliable and stable to be stored on MetaServer
 */
public class DefaultProvideDataNotifier implements ProvideDataNotifier {

    @Autowired
    private DataServerProvideDataNotifier dataServerProvideDataNotifier;

    @Autowired
    private SessionServerProvideDataNotifier sessionServerProvideDataNotifier;

    @Override
    public void notifyProvideDataChange(ProvideDataChangeEvent event) {
        Set<Node.NodeType> notifyTypes = event.getNodeTypes();
        if (notifyTypes.contains(Node.NodeType.DATA)) {
            dataServerProvideDataNotifier.notifyProvideDataChange(event);
        }
        if (notifyTypes.contains(Node.NodeType.SESSION)) {
            sessionServerProvideDataNotifier.notifyProvideDataChange(event);
        }
    }
}
