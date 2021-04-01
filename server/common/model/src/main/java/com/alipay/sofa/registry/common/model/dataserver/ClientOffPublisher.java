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
package com.alipay.sofa.registry.common.model.dataserver;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.*;

/**
 * request to remove data of specific clients immediately
 *
 * @author qian.lqlq
 * @version $Id: ClientOffPublisher.java, v 0.1 2017-12-01 15:48 qian.lqlq Exp $
 */
public final class ClientOffPublisher implements Serializable {
  private static final long serialVersionUID = -3547806571058756207L;

  private final ConnectId connectId;
  private final Map<String, Map<String, RegisterVersion>> publisherMap = Maps.newHashMap();

  public ClientOffPublisher(ConnectId connectId) {
    this.connectId = connectId;
  }

  public void addPublisher(Publisher publisher) {
    Map<String, RegisterVersion> publishers =
        publisherMap.computeIfAbsent(publisher.getDataInfoId(), k -> Maps.newHashMap());
    publishers.put(publisher.getRegisterId(), publisher.registerVersion());
  }

  public ConnectId getConnectId() {
    return connectId;
  }

  public Map<String, Map<String, RegisterVersion>> getPublisherMap() {
    return publisherMap;
  }

  public boolean isEmpty() {
    return publisherMap.isEmpty();
  }

  @Override
  public String toString() {
    return "ClientOff{" + "connId=" + connectId + ", pubs=" + publisherMap + '}';
  }
}
