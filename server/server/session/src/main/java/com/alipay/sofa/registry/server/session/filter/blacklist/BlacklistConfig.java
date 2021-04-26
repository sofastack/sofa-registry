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
package com.alipay.sofa.registry.server.session.filter.blacklist;

import java.io.Serializable;
import java.util.List;

/**
 * @author shangyu.wh
 * @version 1.0: BlacklistConfig.java, v 0.1 2019-06-19 17:23 shangyu.wh Exp $
 */
public class BlacklistConfig implements Serializable {

  /** UID */
  private static final long serialVersionUID = -7607561981062791932L;

  /** blacklist type */
  private String type;

  /** match type */
  @SuppressWarnings("rawtypes")
  private List<MatchType> matchTypes;

  /**
   * Getter method for property <tt>type</tt>.
   *
   * @return property value of type
   */
  public String getType() {
    return type;
  }

  /**
   * Setter method for property <tt>type</tt>.
   *
   * @param type value to be assigned to property type
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Getter method for property <tt>matchTypes</tt>.
   *
   * @return property value of matchTypes
   */
  @SuppressWarnings("rawtypes")
  public List<MatchType> getMatchTypes() {
    return matchTypes;
  }

  /**
   * Setter method for property <tt>matchTypes</tt>.
   *
   * @param matchTypes value to be assigned to property matchTypes
   */
  @SuppressWarnings("rawtypes")
  public void setMatchTypes(List<MatchType> matchTypes) {
    this.matchTypes = matchTypes;
  }

  @Override
  public String toString() {
    return "BlacklistConfig{" + "type='" + type + '\'' + ", match=" + matchTypes + '}';
  }
}
