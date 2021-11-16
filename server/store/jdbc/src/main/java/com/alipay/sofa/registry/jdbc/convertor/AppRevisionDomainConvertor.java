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
package com.alipay.sofa.registry.jdbc.convertor;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: AppRevisionDomainConvertor.java, v 0.1 2021年01月18日 19:00 xiaojian.xj Exp $
 */
public class AppRevisionDomainConvertor {

  public static final TypeReference<HashMap<String, List<String>>> BASE_FORMAT =
      new TypeReference<HashMap<String, List<String>>>() {};
  public static final TypeReference<HashMap<String, AppRevisionInterface>> SERVICE_FORMAT =
      new TypeReference<HashMap<String, AppRevisionInterface>>() {};

  public static AppRevisionDomain convert2Domain(String dataCenter, AppRevision appRevision) {
    if (appRevision == null) {
      return null;
    }
    AppRevisionDomain domain = new AppRevisionDomain();
    domain.setAppName(appRevision.getAppName());
    domain.setDataCenter(dataCenter);
    domain.setRevision(appRevision.getRevision());
    domain.setClientVersion(appRevision.getClientVersion());
    domain.setBaseParams(JsonUtils.writeValueAsString(appRevision.getBaseParams()));
    domain.setServiceParams(JsonUtils.writeValueAsString(appRevision.getInterfaceMap()));
    domain.setDeleted(appRevision.isDeleted());
    return domain;
  }

  public static AppRevision convert2Revision(AppRevisionDomain domain) {

    if (domain == null) {
      return null;
    }

    AppRevision appRevision = new AppRevision();
    appRevision.setId(domain.getId());
    appRevision.setDataCenter(domain.getDataCenter());
    appRevision.setAppName(domain.getAppName());
    appRevision.setRevision(domain.getRevision());
    appRevision.setClientVersion(domain.getClientVersion());
    appRevision.setBaseParams(JsonUtils.read(domain.getBaseParams(), BASE_FORMAT));
    appRevision.setInterfaceMap(JsonUtils.read(domain.getServiceParams(), SERVICE_FORMAT));
    appRevision.setLastHeartbeat(domain.getGmtModify());
    appRevision.setDeleted(domain.isDeleted());
    return appRevision;
  }

  public static List<AppRevision> convert2Revisions(List<AppRevisionDomain> domains) {
    if (CollectionUtils.isEmpty(domains)) {
      return Collections.emptyList();
    }

    List<AppRevision> revisions = new ArrayList<>();
    for (AppRevisionDomain domain : domains) {
      AppRevision revision = convert2Revision(domain);
      if (revision != null) {
        revisions.add(revision);
      }
    }
    return revisions;
  }
}
