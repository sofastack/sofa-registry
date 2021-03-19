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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

public class ClientOffPublishers {
  private final ConnectId connectId;
  private final List<Publisher> publishers;

  public ClientOffPublishers(ConnectId connectId, List<Publisher> publishers) {
    this.connectId = connectId;
    this.publishers = Collections.unmodifiableList(Lists.newArrayList(publishers));
  }

  public ConnectId getConnectId() {
    return connectId;
  }

  public boolean isEmpty() {
    return publishers.isEmpty();
  }

  public List<Publisher> getPublishers() {
    return publishers;
  }
}
