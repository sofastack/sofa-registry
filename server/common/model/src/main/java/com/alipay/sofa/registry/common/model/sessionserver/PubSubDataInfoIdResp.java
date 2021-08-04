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
import java.util.Map;
import java.util.Set;

/**
 * @author xiaojian.xj
 * @version : PubSubDataInfoIdInfo.java, v 0.1 2021年08月04日 10:50 xiaojian.xj Exp $
 */
public class PubSubDataInfoIdResp implements Serializable {

  private Map<String, Set<String>> pubDataInfoIds;

  private Map<String, Set<String>> subDataInfoIds;

  public PubSubDataInfoIdResp() {}

  public PubSubDataInfoIdResp(
      Map<String, Set<String>> pubDataInfoIds, Map<String, Set<String>> subDataInfoIds) {
    this.pubDataInfoIds = pubDataInfoIds;
    this.subDataInfoIds = subDataInfoIds;
  }

  /**
   * Getter method for property <tt>pubDataInfoIds</tt>.
   *
   * @return property value of pubDataInfoIds
   */
  public Map<String, Set<String>> getPubDataInfoIds() {
    return pubDataInfoIds;
  }

  /**
   * Setter method for property <tt>pubDataInfoIds</tt>.
   *
   * @param pubDataInfoIds value to be assigned to property pubDataInfoIds
   */
  public void setPubDataInfoIds(Map<String, Set<String>> pubDataInfoIds) {
    this.pubDataInfoIds = pubDataInfoIds;
  }

  /**
   * Getter method for property <tt>subDataInfoIds</tt>.
   *
   * @return property value of subDataInfoIds
   */
  public Map<String, Set<String>> getSubDataInfoIds() {
    return subDataInfoIds;
  }

  /**
   * Setter method for property <tt>subDataInfoIds</tt>.
   *
   * @param subDataInfoIds value to be assigned to property subDataInfoIds
   */
  public void setSubDataInfoIds(Map<String, Set<String>> subDataInfoIds) {
    this.subDataInfoIds = subDataInfoIds;
  }
}
