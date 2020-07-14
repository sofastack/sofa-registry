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
package com.alipay.sofa.registry.server.data;

import com.alipay.sofa.registry.server.data.bootstrap.CommonConfig;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.renew.LocalDataServerCleanHandler;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.AbstractQueue;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author xiangxu
 * @version : TestDataServerClean.java, v 0.1 2020年07月14日 3:21 下午 xiangxu Exp $
 */

public class TestDataServerClean {
    @Test
    public void testReset() throws NoSuchFieldException, IllegalAccessException {
        LocalDataServerCleanHandler handler = new LocalDataServerCleanHandler();
        Field f1 = handler.getClass().getDeclaredField("dataServerConfig");
        f1.setAccessible(true);
        f1.set(handler, new DataServerConfig(new CommonConfig()));

        handler.reset();
        handler.reset();
        handler.reset();
        handler.reset();
        handler.reset();
        handler.reset();
        Field f2 = handler.getClass().getDeclaredField("EVENT_QUEUE");
        f2.setAccessible(true);
        Collection q = (Collection) f2.get(handler);
        assertEquals(1, q.size());
    }
}