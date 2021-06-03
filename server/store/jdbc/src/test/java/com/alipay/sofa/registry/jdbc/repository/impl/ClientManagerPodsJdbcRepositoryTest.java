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
package com.alipay.sofa.registry.jdbc.repository.impl;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerPods;
import com.alipay.sofa.registry.jdbc.AbstractH2DbTestBase;
import com.alipay.sofa.registry.store.api.meta.ClientManagerPodsRepository;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerPodsJdbcRepositoryTest.java, v 0.1 2021年05月29日 15:25 xiaojian.xj Exp $
 */
public class ClientManagerPodsJdbcRepositoryTest extends AbstractH2DbTestBase {

  @Autowired private ClientManagerPodsRepository clientManagerPodsRepository;

  public static final Set<String> clientOffSet = Sets.newHashSet("1.1.1.1", "2.2.2.2");
  public static final Set<String> clientOpenSet = Sets.newHashSet("2.2.2.2", "3.3.3.3");

  @Test
  public void testClientManager() {
    boolean clientOff =
        clientManagerPodsRepository.clientOff(ClientManagerPodsJdbcRepositoryTest.clientOffSet);
    Assert.assertTrue(clientOff);

    boolean clientOpen =
        clientManagerPodsRepository.clientOpen(ClientManagerPodsJdbcRepositoryTest.clientOpenSet);
    Assert.assertTrue(clientOpen);

    int total = clientManagerPodsRepository.queryTotalCount();
    Assert.assertEquals(3, total);

    List<ClientManagerPods> clientManagerPods = clientManagerPodsRepository.queryAfterThan(-1L);
    List<ClientManagerPods> pods = clientManagerPodsRepository.queryAfterThan(-1L, 1000);
    Assert.assertEquals(total, clientManagerPods.size());
    Assert.assertEquals(total, pods.size());

    for (ClientManagerPods clientManagerPod : clientManagerPods) {
      if (clientOpenSet.contains(clientManagerPod.getAddress())) {
        Assert.assertEquals(ValueConstants.CLIENT_OPEN, clientManagerPod.getOperation());
      } else {
        Assert.assertEquals(ValueConstants.CLIENT_OFF, clientManagerPod.getOperation());
      }
    }
  }
}
