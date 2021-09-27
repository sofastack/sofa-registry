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
package com.alipay.sofa.registry.jdbc.config;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.alipay.sofa.registry.util.SystemUtils;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultCommonConfigBean.java, v 0.1 2021年03月22日 21:06 xiaojian.xj Exp $
 */
public class DefaultCommonConfigBean implements DefaultCommonConfig {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCommonConfigBean.class);

  private String clusterId = SystemUtils.getSystem("nodes.clusterId", "");

  private String recoverClusterId = SystemUtils.getSystem("nodes.recoverClusterId", "");

  private static final String ALL_KEY = "ALL";

  @Autowired private RecoverConfigRepository recoverConfigRepository;

  @PostConstruct
  public void init() throws InterruptedException {
    if (StringUtils.isEmpty(clusterId) || StringUtils.isEmpty(recoverClusterId)) {
      throw new InterruptedException("clusterId or recoverClusterId is empty.");
    }
  }

  @Override
  public String getClusterId(String table) {
    Set<String> keys = recoverConfigRepository.queryKey(table);
    if (!CollectionUtils.isEmpty(keys) && keys.contains(ALL_KEY)) {
      LOG.info("[GetClusterId]propertyTable:{}, clusterId:{}", table, recoverClusterId);
      return recoverClusterId;
    }

    return clusterId;
  }

  @Override
  public String getClusterId(String table, String key) {
    if (StringUtils.isEmpty(table)) {
      throw new IllegalArgumentException("tableName is empty.");
    }
    if (StringUtils.isEmpty(key)) {
      return getClusterId(table);
    }

    Set<String> keys = recoverConfigRepository.queryKey(table);
    if (!CollectionUtils.isEmpty(keys) && keys.contains(key)) {
      LOG.info(
          "[GetClusterId]propertyTable:{}, propertyKey:{}, clusterId:{}",
          table,
          key,
          recoverClusterId);
      return recoverClusterId;
    }

    return clusterId;
  }

  @Override
  public boolean isRecoverCluster() {
    return !StringUtils.equals(clusterId, recoverClusterId);
  }

  @Override
  public String getRecoverClusterId() {
    return recoverClusterId;
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
