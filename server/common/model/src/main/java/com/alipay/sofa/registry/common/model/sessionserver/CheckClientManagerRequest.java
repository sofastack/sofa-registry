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
package com.alipay.sofa.registry.common.model.sessionserver;

import java.io.Serializable;

/**
 * @author xiaojian.xj
 * @version : CheckClientManagerRequest.java, v 0.1 2022年01月04日 20:43 xiaojian.xj Exp $
 */
public class CheckClientManagerRequest implements Serializable {

  private final long expectedVersion;

  public CheckClientManagerRequest(long expectedVersion) {
    this.expectedVersion = expectedVersion;
  }

  /**
   * Getter method for property <tt>expectedVersion</tt>.
   *
   * @return property value of expectedVersion
   */
  public long getExpectedVersion() {
    return expectedVersion;
  }

  @Override
  public String toString() {
    return "CheckClientManagerRequest{" + "expectedVersion=" + expectedVersion + '}';
  }
}
