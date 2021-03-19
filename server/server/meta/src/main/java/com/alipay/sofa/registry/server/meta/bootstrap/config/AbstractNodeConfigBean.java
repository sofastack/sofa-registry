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
package com.alipay.sofa.registry.server.meta.bootstrap.config;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author shangyu.wh
 * @version $Id: AbstractNodeConfigBean.java, v 0.1 2018-05-03 16:14 shangyu.wh Exp $
 */
public abstract class AbstractNodeConfigBean implements NodeConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNodeConfigBean.class);

  protected Map<String /*dataCenterId*/, Collection<String>> metaNodeIP;

  private Map<String, String> dataCenterMetaIPCache = new HashMap<>();

  /**
   * Getter method for property <tt>metaNodeIP</tt>.
   *
   * @return property value of metaNodeIP
   */
  @Override
  public Map<String, Collection<String>> getMetaNodeIP() {
    if (metaNodeIP == null || metaNodeIP.isEmpty()) {
      metaNodeIP = convertToIP(getMetaNode());
    }
    return metaNodeIP;
  }

  private Map<String /*dataCenterId*/, Collection<String>> convertToIP(
      Map<String, Collection<String>> input) {

    Map<String, Collection<String>> ret = new HashMap<>();
    try {

      if (input != null) {
        input.forEach(
            (dataCenter, domains) -> {
              if (domains != null) {
                List<String> ipList = new ArrayList<>();
                domains.forEach(
                    (domain) -> {
                      if (domain != null) {
                        String ip = NetUtil.getIPAddressFromDomain(domain);
                        if (ip == null) {
                          LOGGER.error("Node config convert domain {} error!", domain);
                          throw new RuntimeException(
                              "Node config convert domain {" + domain + "} error!");
                        }
                        ipList.add(ip);
                      }
                    });
                ret.put(dataCenter, ipList);
              }
            });
      }
    } catch (Exception e) {
      LOGGER.error("Node config convert domain error!", e);
      throw new RuntimeException("Node config convert domain error!", e);
    }
    return ret;
  }

  @Override
  public String getMetaDataCenter(String metaIpAddress) {
    if (metaIpAddress == null || metaIpAddress.isEmpty()) {
      LOGGER.error("IpAddress:" + metaIpAddress + " cannot be null!");
      return null;
    }

    String dataCenterRet = dataCenterMetaIPCache.get(metaIpAddress);

    if (dataCenterRet == null || dataCenterRet.isEmpty()) {
      Map<String, Collection<String>> metaList = getMetaNodeIP();

      AtomicReference<String> ret = new AtomicReference<>();
      metaList.forEach(
          (dataCenter, list) -> {
            if (list.contains(metaIpAddress)) {
              ret.set(dataCenter);
            }
          });

      dataCenterRet = ret.get();
    }
    return dataCenterRet;
  }

  @Override
  public Set<String> getDataCenterMetaServers(String dataCenterIn) {
    Map<String, Collection<String>> metaMap = getMetaNodeIP();
    Set<String> metaServerIpSet = new HashSet<>();
    if (metaMap != null && metaMap.size() > 0) {
      Collection<String> list = metaMap.get(dataCenterIn);
      if (list != null) {
        metaServerIpSet.addAll(list);
      }
    }
    return metaServerIpSet;
  }
}
