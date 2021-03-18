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
package com.alipay.sofa.registry.server.meta.resource.filter;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.bootstrap.config.MetaServerConfig;
import com.alipay.sofa.registry.server.shared.env.ServerEnv;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class LeaderAwareFilterTest extends AbstractH2DbTestBase {

    @Autowired
    private LeaderElector leaderElector;

    @Autowired
    private MetaServerConfig metaServerConfig;

    private Channel channel;

    @Test
    public void testFilterWorks() throws TimeoutException, InterruptedException {
        leaderElector.change2Observer();
        channel = JerseyClient.getInstance().connect(new URL(ServerEnv.IP, metaServerConfig.getHttpServerPort()));
        waitConditionUntilTimeOut(()->!leaderElector.amILeader(), 2000);
        Response response = channel
                .getWebTarget()
                .path("stopPushDataSwitch/open")
                .request()
                .get();
        logger.info("[response] {}", response);
    }
}