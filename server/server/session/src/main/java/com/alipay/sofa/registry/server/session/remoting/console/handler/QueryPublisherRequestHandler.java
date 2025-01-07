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
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.sessionserver.QueryPublisherRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/23
 */
public class QueryPublisherRequestHandler extends AbstractConsoleHandler<QueryPublisherRequest> {

  @Autowired protected DataStore sessionDataStore;

  @Override
  public Object doHandle(Channel channel, QueryPublisherRequest request) {
    Collection<Publisher> publishers = sessionDataStore.getDatas(request.getDataInfoId());
    return new GenericResponse().fillSucceed(PublisherUtils.convert(publishers));
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  @Override
  public Class interest() {
    return QueryPublisherRequest.class;
  }

  @VisibleForTesting
  public QueryPublisherRequestHandler setSessionDataStore(DataStore sessionDataStore) {
    this.sessionDataStore = sessionDataStore;
    return this;
  }

  @VisibleForTesting
  public QueryPublisherRequestHandler setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
    return this;
  }
}
