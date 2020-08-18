/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.core.model.Result;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author xiaojian.xj
 * @version $Id: StopPushResource.java, v 0.1 2020年08月14日 17:42 xiaojian.xj Exp $
 */
@Path("stopPush")
public class StopPushResource {

    @Autowired
    private StopPushDataResource stopPushDataResource;


    /**
     * close push data
     */
    @GET
    @Path("true")
    @Produces(MediaType.APPLICATION_JSON)
    public Result closePush() {
        return stopPushDataResource.closePush();
    }


    /**
     * open push data
     */
    @GET
    @Path("false")
    @Produces(MediaType.APPLICATION_JSON)
    public Result openPush() {
        return stopPushDataResource.openPush();
    }

}