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
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : PubSubDataInfoIdRequest.java, v 0.1 2021年08月04日 10:53 xiaojian.xj Exp $
 */
public class PubSubDataInfoIdRequest implements Serializable {

  private Set<String> ips;

  public PubSubDataInfoIdRequest() {}

  public PubSubDataInfoIdRequest(Set<String> ips) {
    this.ips = ips;
  }

  /**
   * Getter method for property <tt>ips</tt>.
   *
   * @return property value of ips
   */
  public Set<String> getIps() {
    return ips;
  }

  /**
   * Setter method for property <tt>ips</tt>.
   *
   * @param ips value to be assigned to property ips
   */
  public void setIps(Set<String> ips) {
    this.ips = ips;
  }
}
