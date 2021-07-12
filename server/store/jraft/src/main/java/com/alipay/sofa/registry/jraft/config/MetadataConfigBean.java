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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataConfigBean.java, v 0.1 2021年02月24日 15:21 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MetadataConfigBean.PRE_FIX)
public class MetadataConfigBean implements MetadataConfig {

  public static final String PRE_FIX = "metadata.server";

  private int interfaceAppsRefreshLimit = 1000;

  private int revisionGcLimit = 100;

  /**
   * Getter method for property <tt>interfaceAppsRefreshLimit</tt>.
   *
   * @return property value of interfaceAppsRefreshLimit
   */
  public int getInterfaceAppsRefreshLimit() {
    return interfaceAppsRefreshLimit;
  }

  /**
   * Setter method for property <tt>interfaceAppsRefreshLimit</tt>.
   *
   * @param interfaceAppsRefreshLimit value to be assigned to property interfaceAppsRefreshLimit
   */
  public void setInterfaceAppsRefreshLimit(int interfaceAppsRefreshLimit) {
    this.interfaceAppsRefreshLimit = interfaceAppsRefreshLimit;
  }

  /**
   * Getter method for property <tt>revisionGcLimit</tt>.
   *
   * @return property value of revisionGcLimit
   */
  @Override
  public int getRevisionGcLimit() {
    return revisionGcLimit;
  }

  /**
   * Setter method for property <tt>revisionGcLimit</tt>.
   *
   * @param revisionGcLimit value to be assigned to property revisionGcLimit
   */
  public void setRevisionGcLimit(int revisionGcLimit) {
    this.revisionGcLimit = revisionGcLimit;
  }
}
