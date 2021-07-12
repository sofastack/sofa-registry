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
package com.alipay.sofa.registry.jraft.config;

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.util.SystemUtils;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultCommonConfigBean.java, v 0.1 2021年03月22日 21:06 xiaojian.xj Exp $
 */
public class DefaultCommonConfigBean implements DefaultCommonConfig {

  private String dataCenter = SystemUtils.getSystem("nodes.localDataCenter", "DefaultDataCenter");

  private String clusterId = SystemUtils.getSystem("nodes.clusterId", "");

  @Override
  public String getClusterId() {
    if (StringUtil.isNotEmpty(clusterId)) {
      return clusterId;
    }
    return dataCenter;
  }

  /**
   * Setter method for property <tt>clusterId</tt>.
   *
   * @param clusterId value to be assigned to property clusterId
   */
  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }
}
