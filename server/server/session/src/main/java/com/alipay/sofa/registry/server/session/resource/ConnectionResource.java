/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
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
        Map<String, Map<String, Publisher>> connectPublishers = sessionDataStore.getConnectPublishers();

        return connectPublishers.keySet();
    }


}