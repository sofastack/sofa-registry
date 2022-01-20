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
 * @version : CheckClientManagerResponse.java, v 0.1 2022年01月19日 19:39 xiaojian.xj Exp $
 */
public class CheckClientManagerResponse implements Serializable {

  private final boolean paasCheck;

  private final long actualVersion;

  public CheckClientManagerResponse(boolean paasCheck, long actualVersion) {
    this.paasCheck = paasCheck;
    this.actualVersion = actualVersion;
  }

  public boolean isPaasCheck() {
    return paasCheck;
  }

  /**
   * Getter method for property <tt>actualVersion</tt>.
   *
   * @return property value of actualVersion
   */
  public long getActualVersion() {
    return actualVersion;
  }

  @Override
  public String toString() {
    return "CheckClientManagerResponse{"
        + "paasCheck="
        + paasCheck
        + ", actualVersion="
        + actualVersion
        + '}';
  }
}
