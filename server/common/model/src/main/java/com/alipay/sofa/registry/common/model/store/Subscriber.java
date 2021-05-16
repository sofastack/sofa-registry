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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.ElementType;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.util.StringFormatter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

/**
 * @author shangyu.wh
 * @version $Id: Subscriber.java, v 0.1 2017-11-30 16:03 shangyu.wh Exp $
 */
public class Subscriber extends BaseInfo {

  /** UID */
  private static final long serialVersionUID = 98433360274932292L;
  /** */
  private ScopeEnum scope;
  /** */
  private ElementType elementType;
  /** */

  /** last push context */
  private final Map<String /*dataCenter*/, PushContext> lastPushContexts = new HashMap<>(4);

  /**
   * Getter method for property <tt>scope</tt>.
   *
   * @return property value of scope
   */
  public ScopeEnum getScope() {
    return scope;
  }

  /**
   * Setter method for property <tt>scope</tt>.
   *
   * @param scope value to be assigned to property scope
   */
  public void setScope(ScopeEnum scope) {
    this.scope = scope;
  }

  public ElementType getElementType() {
    return elementType;
  }

  // check the version
  public synchronized boolean checkVersion(String dataCenter, long version) {
    final PushContext ctx = lastPushContexts.get(dataCenter);
    if (ctx == null) {
      return true;
    }
    return ctx.pushVersion < version;
  }

  public synchronized boolean checkAndUpdateVersion(String dataCenter, long pushVersion) {
    final PushContext ctx = lastPushContexts.computeIfAbsent(dataCenter, k -> new PushContext());

    if (ctx.pushVersion < pushVersion) {
      ctx.pushVersion = pushVersion;
      return true;
    }
    return false;
  }

  public synchronized boolean hasPushed() {
    // TODO now not care multi-datacenter
    if (lastPushContexts.isEmpty()) {
      return false;
    }
    for (PushContext ctx : lastPushContexts.values()) {
      if (ctx.pushVersion != 0) {
        return true;
      }
    }
    return false;
  }

  public String shortDesc() {
    final StringBuilder sb = new StringBuilder(256);
    sb.append("dataInfoId=").append(getDataInfoId()).append(", ");
    sb.append("registerId=").append(getRegisterId()).append(", ");
    sb.append("scope=").append(getScope()).append(", ");
    sb.append("sourceAddress=").append(getSourceAddress().getAddressString());
    return sb.toString();
  }

  /**
   * Setter method for property <tt>elementType</tt>.
   *
   * @param elementType value to be assigned to property elementType
   */
  public void setElementType(ElementType elementType) {
    this.elementType = elementType;
  }

  @Override
  @JsonIgnore
  public DataType getDataType() {
    return DataType.SUBSCRIBER;
  }

  @Override
  protected String getOtherInfo() {
    final StringBuilder sb = new StringBuilder("scope=");
    sb.append(scope).append(",");
    sb.append("elementType=").append(elementType).append(",");
    sb.append("ctx=").append(lastPushContexts);
    return sb.toString();
  }

  public String printPushContext() {
    final StringBuilder sb = new StringBuilder(128);
    return sb.append(lastPushContexts).toString();
  }

  public synchronized long getPushVersion(String dataCenter) {
    PushContext ctx = lastPushContexts.get(dataCenter);
    return ctx == null ? 0 : ctx.pushVersion;
  }

  /**
   * change subscriber word cache
   *
   * @param subscriber
   * @return
   */
  public static Subscriber internSubscriber(Subscriber subscriber) {
    subscriber.setDataInfoId(subscriber.getDataInfoId());
    subscriber.setInstanceId(subscriber.getInstanceId());
    subscriber.setGroup(subscriber.getGroup());
    subscriber.setDataId(subscriber.getDataId());
    subscriber.setCell(subscriber.getCell());
    subscriber.setProcessId(subscriber.getProcessId());
    subscriber.setAppName(subscriber.getAppName());

    return subscriber;
  }

  private static class PushContext {
    long pushVersion;

    @Override
    public String toString() {
      return StringFormatter.format("PushCtx{{}}", pushVersion);
    }
  }
}
