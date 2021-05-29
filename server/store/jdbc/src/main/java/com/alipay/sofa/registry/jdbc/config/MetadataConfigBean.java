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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataConfigBean.java, v 0.1 2021年02月24日 15:21 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = MetadataConfigBean.PRE_FIX)
public class MetadataConfigBean implements MetadataConfig {

  public static final String PRE_FIX = "metadata.server";

  private int revisionRenewIntervalMinutes = 60 * 3;
  private int interfaceAppsIndexRenewIntervalMinutes = 60 * 3;

  public int getRevisionRenewIntervalMinutes() {
    return revisionRenewIntervalMinutes;
  }

  public void setRevisionRenewIntervalMinutes(int revisionRenewIntervalMinutes) {
    this.revisionRenewIntervalMinutes = revisionRenewIntervalMinutes;
  }

  public int getInterfaceAppsIndexRenewIntervalMinutes() {
    return interfaceAppsIndexRenewIntervalMinutes;
  }

  public void setInterfaceAppsIndexRenewIntervalMinutes(
      int interfaceAppsIndexRenewIntervalMinutes) {
    this.interfaceAppsIndexRenewIntervalMinutes = interfaceAppsIndexRenewIntervalMinutes;
  }
}
