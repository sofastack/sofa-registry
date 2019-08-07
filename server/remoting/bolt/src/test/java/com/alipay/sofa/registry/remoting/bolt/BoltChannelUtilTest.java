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
package com.alipay.sofa.registry.remoting.bolt;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.DefaultBizContext;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.RemotingContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuanbei
 * @since 2019/3/26
 */
public class BoltChannelUtilTest {
    @Test
    public void testGetBoltCustomSerializer() {
        Assert.assertNull(BoltChannelUtil.getBoltCustomSerializer(new MockChannel()));
        BoltChannel boltChannel = new BoltChannel();
        InvokeContext invokeContext = new InvokeContext();
        invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, new Object());
        RemotingContext remotingContext = new RemotingContext(new MockChannelHandlerContext(),
            invokeContext, false, new ConcurrentHashMap<>());
        BizContext bizContext = new DefaultBizContext(remotingContext);
        boltChannel.setBizContext(bizContext);
        boolean isException = false;
        try {
            BoltChannelUtil.getBoltCustomSerializer(boltChannel);
        } catch (Throwable r) {
            isException = true;
        }
        Assert.assertTrue(isException);
        invokeContext.put(InvokeContext.BOLT_CUSTOM_SERIALIZER, new Byte("3"));
        Assert.assertEquals(new Byte("3"), BoltChannelUtil.getBoltCustomSerializer(boltChannel));
    }
}
