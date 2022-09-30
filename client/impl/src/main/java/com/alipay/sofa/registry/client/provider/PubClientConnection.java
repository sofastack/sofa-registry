package com.alipay.sofa.registry.client.provider;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.remoting.ClientConnection;
import com.alipay.sofa.registry.client.remoting.ServerManager;

import java.util.List;
import java.util.Map;

/**
 * @author liqiuliang
 * @create 2022-09-30 20:08
 */
public class PubClientConnection extends ClientConnection {
    private Connection pubClientConnection;

    /**
     * Instantiates a new Client connection.
     *
     * @param serverManager               the server manager
     * @param userProcessorList           the user processor list
     * @param connectionEventProcessorMap the connection event processor map
     * @param registerCache
     * @param config                      the config
     */
    public PubClientConnection(ServerManager serverManager, List<UserProcessor> userProcessorList, Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap, RegisterCache registerCache, RegistryClientConfig config) {
        super(serverManager, userProcessorList, connectionEventProcessorMap, registerCache, config);
    }

}
