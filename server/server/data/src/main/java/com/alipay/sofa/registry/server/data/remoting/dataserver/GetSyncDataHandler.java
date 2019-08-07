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
package com.alipay.sofa.registry.server.data.remoting.dataserver;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.CallbackHandler;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.exchange.message.Request;
import com.alipay.sofa.registry.server.data.remoting.DataNodeExchanger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author qian.lqlq
 * @version $Id: GetSyncDataHandler.java, v 0.1 2018-03-08 14:18 qian.lqlq Exp $
 */
public class GetSyncDataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetSyncDataHandler.class);

    @Autowired
    private DataNodeExchanger   dataNodeExchanger;

    /**
     *
     *
     * @param callback
     */
    public void syncData(SyncDataCallback callback) {
        int tryCount = callback.getRetryCount();
        if (tryCount > 0) {
            try {
                callback.setRetryCount(--tryCount);
                dataNodeExchanger.request(new Request() {
                    @Override
                    public Object getRequestBody() {
                        return callback.getRequest();
                    }

                    @Override
                    public URL getRequestUrl() {
                        return new URL(callback.getConnection().getRemoteIP(), callback
                            .getConnection().getRemotePort());
                    }

                    @Override
                    public CallbackHandler getCallBackHandler() {
                        return new CallbackHandler() {
                            @Override
                            public void onCallback(Channel channel, Object message) {
                                callback.onResponse(message);
                            }

                            @Override
                            public void onException(Channel channel, Throwable exception) {
                                callback.onException(exception);
                            }
                        };
                    }
                });
            } catch (Exception e) {
                LOGGER.error("[GetSyncDataHandler] send sync data request failed", e);
            }
        } else {
            LOGGER.info("[GetSyncDataHandler] sync data retry for three times");
        }
    }

}