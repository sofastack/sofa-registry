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
import com.alipay.sofa.registry.jdbc.domain.ClientManagerAddressDomain;
import com.alipay.sofa.registry.jdbc.informer.DbEntryContainer;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressContainer.java, v 0.1 2021年06月16日 19:54 xiaojian.xj Exp $
 */
public class ClientManagerAddressContainer implements DbEntryContainer<ClientManagerAddressDomain> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientManagerAddressContainer.class);

  private final Set<String> clientOffData = Sets.newConcurrentHashSet();

  @Override
  public void onEntry(ClientManagerAddressDomain clientManagerAddress) {
    switch (clientManagerAddress.getOperation()) {
      case ValueConstants.CLIENT_OFF:
        clientOffData.add(clientManagerAddress.getAddress());
        break;
      case ValueConstants.CLIENT_OPEN:
      case ValueConstants.REDUCE:
        clientOffData.remove(clientManagerAddress.getAddress());
        break;
      default:
        LOGGER.error("error operation type: {}", clientManagerAddress);
        break;
    }
  }

  public Set<String> queryClientOffData() {
    return clientOffData;
  }
}
