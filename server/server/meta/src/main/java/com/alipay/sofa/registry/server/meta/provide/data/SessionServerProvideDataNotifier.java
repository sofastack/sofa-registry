package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.lease.SessionManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
public class SessionServerProvideDataNotifier extends AbstractProvideDataNotifier<SessionNode> implements ProvideDataNotifier {

    @Autowired
    private NodeExchanger sessionNodeExchanger;

    @Autowired
    private AbstractServerHandler sessionConnectionHandler;

    @Autowired
    private SessionManager sessionManager;

    @Override
    protected NodeExchanger getNodeExchanger() {
        return sessionNodeExchanger;
    }

    @Override
    protected List<SessionNode> getNodes() {
        return sessionManager.getClusterMembers();
    }

    @Override
    protected NodeConnectManager getNodeConnectManager() {
        if (!(sessionConnectionHandler instanceof NodeConnectManager)) {
            logger.error("sessionConnectionHandler inject is not NodeConnectManager instance!");
            throw new RuntimeException(
                    "sessionConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) sessionConnectionHandler;
    }
}
