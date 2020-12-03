package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.remoting.exchange.NodeExchanger;
import com.alipay.sofa.registry.server.meta.lease.DataServerManager;
import com.alipay.sofa.registry.server.meta.remoting.connection.NodeConnectManager;
import com.alipay.sofa.registry.server.meta.remoting.handler.AbstractServerHandler;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
public class DataServerProvideDataNotifier extends AbstractProvideDataNotifier<DataNode> implements ProvideDataNotifier {

    @Autowired
    private NodeExchanger dataNodeExchanger;

    @Autowired
    private AbstractServerHandler dataConnectionHandler;

    @Autowired
    private DataServerManager dataServerManager;

    @Override
    protected NodeExchanger getNodeExchanger() {
        return dataNodeExchanger;
    }

    @Override
    protected List<DataNode> getNodes() {
        return dataServerManager.getClusterMembers();
    }

    @Override
    protected NodeConnectManager getNodeConnectManager() {
        if (!(dataConnectionHandler instanceof NodeConnectManager)) {
            logger.error("dataConnectionHandler inject is not NodeConnectManager instance!");
            throw new RuntimeException(
                    "dataConnectionHandler inject is not NodeConnectManager instance!");
        }

        return (NodeConnectManager) dataConnectionHandler;
    }

    @VisibleForTesting
    DataServerProvideDataNotifier setDataNodeExchanger(NodeExchanger dataNodeExchanger) {
        this.dataNodeExchanger = dataNodeExchanger;
        return this;
    }

    @VisibleForTesting
    DataServerProvideDataNotifier setDataConnectionHandler(AbstractServerHandler dataConnectionHandler) {
        this.dataConnectionHandler = dataConnectionHandler;
        return this;
    }

    @VisibleForTesting
    DataServerProvideDataNotifier setDataServerManager(DataServerManager dataServerManager) {
        this.dataServerManager = dataServerManager;
        return this;
    }
}
