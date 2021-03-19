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
import java.util.Set;

/**
 * @author shangyu.wh
 * @version 1.0: MatchType.java, v 0.1 2019-06-19 17:25 shangyu.wh Exp $
 */
public class MatchType<T> implements Serializable {
  /** UID */
  private static final long serialVersionUID = 4015181538538056685L;

  /** match type */
  private String type;

  /** match patterns */
  private Set<T> patternSet;

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
   * Getter method for property <tt>patternSet</tt>.
   *
   * @return property value of patternSet
   */
  public Set<T> getPatternSet() {
    return patternSet;
  }

  /**
   * Setter method for property <tt>patternSet</tt>.
   *
   * @param patternSet value to be assigned to property patternSet
   */
  public void setPatternSet(Set<T> patternSet) {
    this.patternSet = patternSet;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "MatchType{" + "type='" + type + '\'' + ", patternSet=" + patternSet + '}';
  }
}
