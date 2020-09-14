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
package com.alipay.sofa.registry.server.session.resource;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import com.alipay.sofa.registry.server.session.store.SessionDataStore;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author xiaojian.xj
 * @version $Id: ConnectionResource.java, v 0.1 2020年08月14日 18:01 xiaojian.xj Exp $
 */
@Path("connect")
public class ConnectionResource {

    @Autowired
    private SessionDataStore sessionDataStore;

    @GET
    @Path("query")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> queryConnectIds() {
        Map<String, Map<String, Publisher>> connectPublishers = sessionDataStore
            .getConnectPublishers();

        return connectPublishers.keySet();
    }

}