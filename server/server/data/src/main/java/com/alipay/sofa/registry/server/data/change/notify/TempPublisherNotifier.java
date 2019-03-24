/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.data.change.notify;

import com.alipay.remoting.Connection;
import com.alipay.remoting.InvokeCallback;
import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.sessionserver.DataPushRequest;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.executor.ExecutorFactory;
import com.alipay.sofa.registry.server.data.remoting.sessionserver.SessionServerConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 *
 * @author shangyu.wh
 * @version $Id: TempPublisherNotifier.java, v 0.1 2018-08-29 18:05 shangyu.wh Exp $
 */
public class TempPublisherNotifier implements IDataChangeNotifier {

    private static final Logger            LOGGER   = LoggerFactory
                                                        .getLogger(TempPublisherNotifier.class);

    private static final Executor          EXECUTOR = ExecutorFactory
                                                        .newFixedThreadPool(10,
                                                            TempPublisherNotifier.class
                                                                .getSimpleName());

    @Autowired
    private DataServerConfig               dataServerBootstrapConfig;

    @Autowired
    private Exchange                       boltExchange;

    @Autowired
    private SessionServerConnectionFactory sessionServerConnectionFactory;

    @Override
    public Set<DataSourceTypeEnum> getSuitableSource() {
        Set<DataSourceTypeEnum> set = new HashSet<>();
        set.add(DataSourceTypeEnum.PUB_TEMP);
        return set;
    }

    @Override
    public void notify(Datum datum, Long lastVersion) {
        DataPushRequest request = new DataPushRequest(datum);
        List<Connection> connections = sessionServerConnectionFactory.getConnections();
        for (Connection connection : connections) {
            doNotify(new NotifyPushDataCallback(connection, request));
        }
    }

    private void doNotify(NotifyPushDataCallback notifyPushdataCallback) {

        Connection connection = notifyPushdataCallback.getConnection();
        DataPushRequest request = notifyPushdataCallback.getRequest();
        try {
            Server sessionServer = boltExchange.getServer(dataServerBootstrapConfig.getPort());
            sessionServer.sendCallback(sessionServer.getChannel(connection.getRemoteAddress()),
                request, new CallbackHandler() {

                    @Override
                    public void onCallback(Channel channel, Object message) {
                        notifyPushdataCallback.onResponse(message);
                    }

                    @Override
                    public void onException(Channel channel, Throwable exception) {
                        notifyPushdataCallback.onException(exception);
                    }
                }, dataServerBootstrapConfig.getRpcTimeout());
        } catch (Exception e) {
            LOGGER.error("[TempPublisherNotifier] notify sessionserver {} failed, {}",
                connection.getRemoteIP(), request, e);
        }
    }

    private static class NotifyPushDataCallback implements InvokeCallback {

        private Connection      connection;

        private DataPushRequest request;

        public NotifyPushDataCallback(Connection connection, DataPushRequest request) {
            this.connection = connection;
            this.request = request;
        }

        @Override
        public void onResponse(Object obj) {
            CommonResponse result = (CommonResponse) obj;
            if (result != null && !result.isSuccess()) {
                //doNotify(this);
                LOGGER
                    .error(
                        "[TempPublisherNotifier] notify sessionserver {} not success, request={}, result={}",
                        connection.getRemoteIP(), request, result.getMessage());
            }
        }

        @Override
        public void onException(Throwable e) {
            onResponse(CommonResponse.buildFailedResponse(e.getMessage()));
        }

        @Override
        public Executor getExecutor() {
            return EXECUTOR;
        }

        /**
         * Getter method for property <tt>connection</tt>.
         *
         * @return property value of connection
         */
        public Connection getConnection() {
            return connection;
        }

        /**
         * Getter method for property <tt>request</tt>.
         *
         * @return property value of request
         */
        public DataPushRequest getRequest() {
            return request;
        }
    }
}