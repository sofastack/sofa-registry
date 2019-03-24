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
package com.alipay.sofa.registry.server.meta.test;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.meta.MetaApplication;
import com.alipay.sofa.registry.server.meta.bootstrap.EnableMetaServer;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfig.DecisionMode;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author shangyu.wh
 * @version $Id: HttpTest.java, v 0.1 2018-02-01 17:01 shangyu.wh Exp $
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MetaApplication.class)
@EnableMetaServer
public class HttpTest extends BaseTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("nodes.metaNode", "registry-stable:"
                                             + NetUtil.getLocalAddress().getHostAddress());
        System.setProperty("nodes.localDataCenter", "registry-stable");
        FileUtils.deleteDirectory(new File(System.getProperty("user.home") + File.separator
                                           + "raftData"));
    }

    @Test
    public void testDecisionMode() {
        URL url = new URL(NetUtil.getLocalAddress().getHostAddress(), 9615);
        JerseyClient jerseyClient = JerseyClient.getInstance();
        Channel channel = jerseyClient.connect(url);
        Result response = channel.getWebTarget().path("decisionMode").request()
            .post(Entity.entity(DecisionMode.OFF, MediaType.APPLICATION_JSON), Result.class);

        assertTrue(response.isSuccess());
    }

    @Test
    public void testConnect() throws Exception {
        URL targetUrl = new URL(NetUtil.getLocalAddress().getHostAddress(), 9615);
        java.net.URL urlnet = new java.net.URL("http", targetUrl.getIpAddress(),
            targetUrl.getPort(), "");
        URI uri = urlnet.toURI();
        java.net.URL getUrl = UriBuilder.fromUri(uri).path("decisionMode").build().toURL();

        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        try {
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);

            assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }
}