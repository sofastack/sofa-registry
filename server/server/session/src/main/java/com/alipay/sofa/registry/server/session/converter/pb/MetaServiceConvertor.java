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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.MetaService;
import com.alipay.sofa.registry.common.model.client.pb.StringList;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author xiaojian.xj
 * @version $Id: MetaServiceConvertor.java, v 0.1 2021年02月04日 22:27 xiaojian.xj Exp $
 */
public class MetaServiceConvertor {

  public static MetaService convert2Pb(AppRevisionInterface service) {

    MetaService.Builder serviceBuilder = MetaService.newBuilder();
    serviceBuilder.setId(service.getId());

    for (Entry<String, List<String>> entry :
        Optional.ofNullable(service.getServiceParams()).orElse(new HashMap<>()).entrySet()) {
      StringList.Builder listBuilder = StringList.newBuilder().addAllValues(entry.getValue());
      serviceBuilder.putParams(entry.getKey(), listBuilder.build());
    }

    return serviceBuilder.build();
  }

  public static AppRevisionInterface convert2Java(MetaService metaService) {
    AppRevisionInterface appRevisionInterface = new AppRevisionInterface();
    appRevisionInterface.setId(metaService.getId());
    Map<String, List<String>> serviceParams = new HashMap<>();
    for (Entry<String, StringList> paramEntry :
        Optional.ofNullable(metaService.getParamsMap()).orElse(new HashMap<>()).entrySet()) {
      StringList value = paramEntry.getValue();
      serviceParams.put(
          paramEntry.getKey(), value.getValuesList().subList(0, value.getValuesCount()));
    }
    appRevisionInterface.setServiceParams(serviceParams);

    return appRevisionInterface;
  }
}
