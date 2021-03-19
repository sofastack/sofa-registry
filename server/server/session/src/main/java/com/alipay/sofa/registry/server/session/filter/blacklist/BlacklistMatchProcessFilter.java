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
package com.alipay.sofa.registry.server.session.filter.blacklist;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.filter.DataIdMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.IPMatchStrategy;
import com.alipay.sofa.registry.server.session.filter.ProcessFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author shangyu.wh
 * @version 1.0: BlacklistMatchProcessFilter.java, v 0.1 2019-06-19 22:01 shangyu.wh Exp $
 */
public class BlacklistMatchProcessFilter implements ProcessFilter<BaseInfo> {

  @Autowired private DataIdMatchStrategy dataIdMatchStrategy;

  @Autowired private IPMatchStrategy ipMatchStrategy;

  @Autowired private BlacklistManager blacklistManager;

  @Autowired private SessionServerConfig sessionServerConfig;

  @Override
  public boolean match(BaseInfo storeData) {

    final List<BlacklistConfig> configList = blacklistManager.getBlacklistConfigList();

    // empty list proceed
    if (null == configList || configList.size() == 0) {
      return false;
    }

    URL url = storeData.getSourceAddress();

    if (url != null) {

      switch (storeData.getDataType()) {
        case PUBLISHER:
          if (dataIdMatchStrategy.match(
              storeData.getDataId(), () -> sessionServerConfig.getBlacklistPubDataIdRegex())) {
            return ipMatchStrategy.match(
                url.getIpAddress(), () -> BlacklistConstants.FORBIDDEN_PUB);
          }
        case SUBSCRIBER:
          if (dataIdMatchStrategy.match(
              storeData.getDataId(), () -> sessionServerConfig.getBlacklistSubDataIdRegex())) {
            return ipMatchStrategy.match(
                url.getIpAddress(), () -> BlacklistConstants.FORBIDDEN_SUB_BY_PREFIX);
          }
        default:
          return false;
      }
    }
    return false;
  }

  /**
   * Getter method for property <tt>dataIdMatchStrategy</tt>.
   *
   * @return property value of dataIdMatchStrategy
   */
  public DataIdMatchStrategy getDataIdMatchStrategy() {
    return dataIdMatchStrategy;
  }

  /**
   * Setter method for property <tt>dataIdMatchStrategy</tt>.
   *
   * @param dataIdMatchStrategy value to be assigned to property dataIdMatchStrategy
   */
  public void setDataIdMatchStrategy(DataIdMatchStrategy dataIdMatchStrategy) {
    this.dataIdMatchStrategy = dataIdMatchStrategy;
  }

  /**
   * Getter method for property <tt>ipMatchStrategy</tt>.
   *
   * @return property value of ipMatchStrategy
   */
  public IPMatchStrategy getIpMatchStrategy() {
    return ipMatchStrategy;
  }

  /**
   * Setter method for property <tt>ipMatchStrategy</tt>.
   *
   * @param ipMatchStrategy value to be assigned to property ipMatchStrategy
   */
  public void setIpMatchStrategy(IPMatchStrategy ipMatchStrategy) {
    this.ipMatchStrategy = ipMatchStrategy;
  }

  /**
   * Getter method for property <tt>blacklistManager</tt>.
   *
   * @return property value of blacklistManager
   */
  public BlacklistManager getBlacklistManager() {
    return blacklistManager;
  }

  /**
   * Setter method for property <tt>blacklistManager</tt>.
   *
   * @param blacklistManager value to be assigned to property blacklistManager
   */
  public void setBlacklistManager(BlacklistManager blacklistManager) {
    this.blacklistManager = blacklistManager;
  }
}
