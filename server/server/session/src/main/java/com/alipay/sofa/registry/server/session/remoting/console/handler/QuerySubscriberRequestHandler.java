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
import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.sessionserver.QuerySubscriberRequest;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.store.Interests;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class QuerySubscriberRequestHandler extends AbstractConsoleHandler<QuerySubscriberRequest> {
  @Autowired protected Interests sessionInterests;

  @Override
  public Object doHandle(Channel channel, QuerySubscriberRequest request) {
    Collection<Subscriber> subscribers;
    if (StringUtils.isNotEmpty(request.getSuberApp()) || request.getLimit() != 0) {
      subscribers =
          sessionInterests.getInterestsByOption(
              request.getDataInfoId(), request.getSuberApp(), request.getLimit());
    } else {
      subscribers = sessionInterests.getInterests(request.getDataInfoId());
    }

    return new GenericResponse().fillSucceed(SubscriberUtils.convert(subscribers));
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  @Override
  public Class interest() {
    return QuerySubscriberRequest.class;
  }
}
