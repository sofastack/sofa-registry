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
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.server.meta.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author xiangxu
 * @version : SessionLoadbalanceSwitchResource.java, v 0.1 2020年06月02日 2:51 下午 xiangxu Exp $
 */

@Path("sessionLoadbalance")
public class SessionLoadbalanceResource {
    @Autowired
    private Registry metaServerRegistry;

    @POST
    @Path("run")
    @Produces(MediaType.APPLICATION_JSON)
    public Object run(@FormParam("maxDisconnect") @DefaultValue("0") String maxDisconnect) {
        return metaServerRegistry.sessionLoadbalance(Integer.parseInt(maxDisconnect));
    }
}