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
package com.alipay.sofa.registry.store.api.config;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author xiaojian.xj
 * @version $Id: DefaultCommonConfigBean.java, v 0.1 2021年03月22日 21:06 xiaojian.xj Exp $
 */
public class DefaultCommonConfigBean implements DefaultCommonConfig {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCommonConfigBean.class);

  @Value("${nodes.localDataCenter}")
  private String dataCenter;

  @Value("${nodes.clusterId:}")
  private String clusterId;

  @Value("${persistence.profile.active:jdbc}")
  private String persistenceProfileActive;

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
  @VisibleForTesting
  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }

  /**
   * Setter method for property <tt>recoverClusterId</tt>.
   *
   * @param recoverClusterId value to be assigned to property recoverClusterId
   */
  @VisibleForTesting
  public void setRecoverClusterId(String recoverClusterId) {
    this.recoverClusterId = recoverClusterId;
  }

  public boolean isJdbc() {
    return !SpringContext.META_STORE_API_RAFT.equals(persistenceProfileActive);
  }
}
