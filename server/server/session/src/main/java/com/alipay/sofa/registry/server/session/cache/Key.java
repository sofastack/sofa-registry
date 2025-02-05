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
package com.alipay.sofa.registry.server.session.cache;

import com.alipay.sofa.registry.cache.Sizer;
import com.alipay.sofa.registry.util.StringFormatter;

/**
 * cache query condition key
 *
 * @author shangyu.wh
 * @version $Id: Key.java, v 0.1 2017-12-06 15:52 shangyu.wh Exp $
 */
public final class Key implements Sizer {

  private final String entityName;
  private final String hashKey;
  private final EntityType entityType;

  /**
   * construct func
   *
   * @param entityName entityName
   * @param entityType entityType
   */
  public Key(String entityName, EntityType entityType) {
    this.entityName = entityName;
    this.entityType = entityType;
    this.hashKey = this.entityName + this.entityType.getUniqueKey();
  }

  /**
   * Getter method for property <tt>entityName</tt>.
   *
   * @return property value of entityName
   */
  public String getEntityName() {
    return entityName;
  }

  /**
   * Getter method for property <tt>hashKey</tt>.
   *
   * @return property value of hashKey
   */
  public String getHashKey() {
    return hashKey;
  }

  /**
   * Getter method for property <tt>entityType</tt>.
   *
   * @return property value of entityType
   */
  public EntityType getEntityType() {
    return entityType;
  }

  @Override
  public int hashCode() {
    String hashKey = getHashKey();
    return hashKey.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Key) {
      return getHashKey().equals(((Key) other).getHashKey());
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return StringFormatter.format("Key{{}, type={}}", entityName, entityType);
  }

  public int size() {
    return entityName.length() + hashKey.length();
  }
}
