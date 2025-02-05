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
package com.alipay.sofa.registry.server.session.remoting.console.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.FilterSubscriberIPsRequest;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.store.SubscriberStore;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public class FilterSubscriberIPsHandler extends AbstractConsoleHandler<FilterSubscriberIPsRequest> {
  @Autowired protected SubscriberStore subscriberStore;

  @Override
  public Class interest() {
    return FilterSubscriberIPsRequest.class;
  }

  @Override
  public Object doHandle(Channel channel, FilterSubscriberIPsRequest request) {
    Map<String, Collection<Subscriber>> subscribers =
        subscriberStore.query(request.getGroup(), request.getIpLimit());
    if (subscribers == null || subscribers.size() == 0) {
      return new GenericResponse().fillSucceed(Collections.emptyMap());
    }
    Map<String, List<String>> ret = Maps.newHashMapWithExpectedSize(subscribers.size());
    for (Map.Entry<String, Collection<Subscriber>> entry : subscribers.entrySet()) {
      ret.put(
          entry.getKey(),
          entry.getValue().stream()
              .map(subscriber -> subscriber.getSourceAddress().getIpAddress())
              .collect(Collectors.toList()));
    }
    return new GenericResponse().fillSucceed(ret);
  }
}
