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
import com.alipay.sofa.registry.common.model.metaserver.ClientManagerAddress.AddressVersion;
import com.alipay.sofa.registry.jdbc.domain.ClientManagerAddressDomain;
import com.alipay.sofa.registry.jdbc.informer.DbEntryContainer;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.glassfish.jersey.internal.guava.Sets;

/**
 * @author xiaojian.xj
 * @version $Id: ClientManagerAddressContainer.java, v 0.1 2021年06月16日 19:54 xiaojian.xj Exp $
 */
public class ClientManagerAddressContainer implements DbEntryContainer<ClientManagerAddressDomain> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientManagerAddressContainer.class);

  private final Map<String, AddressVersion> clientOffData = Maps.newConcurrentMap();

  private volatile Set<String> reduces = Sets.newHashSet();

  @Override
  public void onEntry(ClientManagerAddressDomain clientManagerAddress) {
    switch (clientManagerAddress.getOperation()) {
      case ValueConstants.CLIENT_OFF:
        clientOffData.put(
            clientManagerAddress.getAddress(),
            new AddressVersion(
                clientManagerAddress.getGmtCreateUnixMillis(),
                clientManagerAddress.getAddress(),
                clientManagerAddress.isSub()));
        reduces.remove(clientManagerAddress.getAddress());
        break;
      case ValueConstants.CLIENT_OPEN:
        clientOffData.remove(clientManagerAddress.getAddress());
        reduces.remove(clientManagerAddress.getAddress());
        break;
      case ValueConstants.REDUCE:
        clientOffData.remove(clientManagerAddress.getAddress());
        reduces.add(clientManagerAddress.getAddress());
        break;
      default:
        LOGGER.error("error operation type: {}", clientManagerAddress);
        break;
    }
  }

  public ClientManagerAddress queryClientManagerAddress() {
    return new ClientManagerAddress(
        Maps.newHashMap(clientOffData), Collections.unmodifiableSet(reduces));
  }

  public class ClientManagerAddress {
    private final Map<String, AddressVersion> clientOffData;

    private final Set<String> reduces;

    public ClientManagerAddress(Map<String, AddressVersion> clientOffData, Set<String> reduces) {
      this.clientOffData = clientOffData;
      this.reduces = reduces;
    }

    /**
     * Getter method for property <tt>clientOffData</tt>.
     *
     * @return property value of clientOffData
     */
    public Map<String, AddressVersion> getClientOffData() {
      return clientOffData;
    }

    /**
     * Getter method for property <tt>reduces</tt>.
     *
     * @return property value of reduces
     */
    public Set<String> getReduces() {
      return reduces;
    }
  }
}
