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

import sun.misc.ObjectInputFilter;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.reflect.Field;

/**
 *
 * @author shangyu.wh
 * @version 1.0: SessionSerialFilterResource.java, v 0.1 2019-07-09 15:24 shangyu.wh Exp $
 */
@Path("serialFilter")
public class SessionSerialFilterResource {

    @POST
    @Path("blacklist/jvm")
    @Produces(MediaType.APPLICATION_JSON)
    public void setJvmSerialFilter(String blacklist) {
        try {
            Field serialFilterField = ObjectInputFilter.Config.class
                .getDeclaredField("serialFilter");
            serialFilterField.setAccessible(true);
            serialFilterField.set(ObjectInputFilter.Config.class,
                ObjectInputFilter.Config.createFilter(blacklist));
            serialFilterField.setAccessible(false);
        } catch (Throwable e) {
            System.err //NOPMD
                .println("Current JDK do not support update black list, please update JDK if the feature is needed."); //NOPMD
            e.printStackTrace(); //NOPMD
        }
    }

}