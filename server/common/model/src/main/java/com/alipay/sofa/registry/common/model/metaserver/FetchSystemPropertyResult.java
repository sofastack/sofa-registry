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
package com.alipay.sofa.registry.common.model.metaserver;

import java.io.Serializable;

/**
 * @author xiaojian.xj
 * @version $Id: FetchSystemPropertyResult.java, v 0.1 2021年05月12日 21:41 xiaojian.xj Exp $
 */
public class FetchSystemPropertyResult implements Serializable {

  private static final ProvideData INIT = null;

  private final boolean versionUpgrade;

  private final ProvideData provideData;

  public FetchSystemPropertyResult(boolean versionUpgrade) {
    this.versionUpgrade = versionUpgrade;
    this.provideData = INIT;
  }

  public FetchSystemPropertyResult(boolean versionUpgrade, ProvideData provideData) {
    this.versionUpgrade = versionUpgrade;
    this.provideData = provideData;
  }

  public boolean isVersionUpgrade() {
    return versionUpgrade;
  }

  /**
   * Getter method for property <tt>provideData</tt>.
   *
   * @return property value of provideData
   */
  public ProvideData getProvideData() {
    return provideData;
  }

  @Override
  public String toString() {
    return "FetchSystemPropertyResult{"
        + "versionUpgrade="
        + versionUpgrade
        + ", provideData="
        + provideData
        + '}';
  }
}
