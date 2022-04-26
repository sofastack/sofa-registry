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
package com.alipay.sofa.registry.test.mapper;

import com.alipay.sofa.registry.server.session.mapper.ConnectionMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ruoshan
 * @since 5.4.1
 */
public class ConnectionMapperTest {

  @Test
  public void simpleTestForMap() {
    String connectId = "1.1.1.1:1234";
    String clientIp = "2.2.2.2";
    ConnectionMapper connectionMapper = new ConnectionMapper();
    // add

    connectionMapper.add(connectId, clientIp);
    Assert.assertTrue(connectionMapper.contains(connectId));
    Assert.assertEquals(clientIp, connectionMapper.get(connectId));
    // remove
    connectionMapper.remove(connectId);
    Assert.assertFalse(connectionMapper.contains(connectId));
  }
}
