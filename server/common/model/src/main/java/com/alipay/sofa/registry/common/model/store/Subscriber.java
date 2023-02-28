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
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.alipay.sofa.registry.util.StringFormatter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

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
  private String[] acceptEncodes;

  private boolean acceptMulti;

  /** last push context */
  private Map<String /*dataCenter*/, PushContext> lastPushContexts;

  private int pushedFailCount = 0;

  private long lastPushedFailTimeStamp = 0;

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

  public boolean acceptMulti() {
    return this.acceptMulti;
  }

  /**
   * Getter method for property <tt>lastPushContexts</tt>.
   *
   * @return property value of lastPushContexts
   */
  public Map<String, PushContext> getLastPushContexts() {
    return lastPushContexts;
  }

  /**
   * Setter method for property <tt>acceptMulti</tt>.
   *
   * @param acceptMulti value to be assigned to property acceptMulti
   */
  public void setAcceptMulti(boolean acceptMulti) {
    this.acceptMulti = acceptMulti;
  }

  private PushContext getPushContext(String dataCenter) {
    PushContext ctx = null;
    if (lastPushContexts == null) {
      ctx = new PushContext();
      this.lastPushContexts = Collections.singletonMap(dataCenter, ctx);
    } else {
      ctx = lastPushContexts.get(dataCenter);
      if (ctx == null) {
        // multi dataCenter, replace the singletonMap
        if (!(lastPushContexts instanceof HashMap)) {
          this.lastPushContexts = new HashMap<>(lastPushContexts);
        }
        ctx = new PushContext();
        this.lastPushContexts.put(dataCenter, ctx);
      }
    }
    return ctx;
  }

  // check the version
  public synchronized boolean checkVersion(String dataCenter, long version) {
    final PushContext ctx = getPushContext(dataCenter);
    // emptyVersion != 0, means not care any version update
    return ctx.pushedVersion < version && ctx.emptyVersion == 0;
  }

  /**
   * return true if one of any dataCenter need to update
   *
   * @param versions
   * @return
   */
  public synchronized boolean checkVersion(Map<String, Long> versions) {
    ParaCheckUtil.checkNotEmpty(versions, "versions");
    for (Entry<String, Long> entry : versions.entrySet()) {
      if (checkVersion(entry.getKey(), entry.getValue())) {
        return true;
      }
    }
    return false;
  }

  public synchronized boolean checkAndUpdateCtx(
      Map<String, Long> versions, Map<String, Integer> nums) {
    ParaCheckUtil.checkEquals(versions.keySet(), nums.keySet(), "checkAndUpdateCtx.datacenters");
    for (Entry<String, Long> entry : versions.entrySet()) {
      checkAndUpdateCtx(entry.getKey(), entry.getValue(), nums.get(entry.getKey()));
    }
    return true;
  }

  private synchronized boolean checkAndUpdateCtx(String dataCenter, long pushVersion, int num) {
    final PushContext ctx = getPushContext(dataCenter);

    if (ctx.pushedVersion < pushVersion) {
      ctx.pushedVersion = pushVersion;
      ctx.pushedNum = num;
      pushedFailCount = 0;
      lastPushedFailTimeStamp = 0;
      return true;
    }
    return false;
  }

  public synchronized boolean onPushFail(Map<String, Long> versions) {
    for (Entry<String, Long> entry : versions.entrySet()) {
      if (onPushFail(entry.getKey(), entry.getValue())) {
        return true;
      }
    }
    return false;
  }

  private synchronized boolean onPushFail(String dataCenter, long pushVersion) {
    final PushContext ctx = getPushContext(dataCenter);

    if (ctx.pushedVersion < pushVersion) {
      pushedFailCount += 1;
      lastPushedFailTimeStamp = System.currentTimeMillis();
      return true;
    }
    return false;
  }

  /**
   * return true if all datacenter check skip
   *
   * @param versions
   * @param nums
   * @return
   */
  public synchronized boolean checkSkipPushEmpty(
      Map<String, Long> versions, Map<String, Integer> nums) {
    ParaCheckUtil.checkEquals(versions.keySet(), nums.keySet(), "checkSkipPushEmpty.datacenters");

    for (Entry<String, Long> entry : versions.entrySet()) {
      if (!checkSkipPushEmpty(entry.getKey(), entry.getValue(), nums.get(entry.getKey()))) {
        return false;
      }
    }
    return true;
  }

  public synchronized boolean checkSkipPushEmpty(String dataCenter, long pushVersion, int num) {
    final PushContext ctx = getPushContext(dataCenter);
    long lastPushMaxVersion = ctx.lastMaxPushVersion;
    long lastPushVersion = ctx.lastPushVersion;
    ctx.lastMaxPushVersion = Math.max(lastPushMaxVersion, pushVersion);
    ctx.lastPushVersion = pushVersion;
    if (num > 0) {
      return false;
    }
    if (lastPushMaxVersion <= ValueConstants.DEFAULT_NO_DATUM_VERSION) {
      return false;
    }
    return lastPushVersion == lastPushMaxVersion
        && lastPushMaxVersion == ctx.pushedVersion
        && ctx.pushedNum == 0;
  }

  /**
   * return true if one of any dataCenter need push empty
   *
   * @param dataCenters
   * @return
   */
  public synchronized boolean needPushEmpty(Set<String> dataCenters) {
    for (String dataCenter : dataCenters) {
      if (needPushEmpty(dataCenter)) {
        return true;
      }
    }
    return false;
  }

  public synchronized boolean needPushEmpty(String dataCenter) {
    final PushContext ctx = getPushContext(dataCenter);
    if (ctx.emptyVersion == 0) {
      return false;
    }
    // empty has mark, last push.num is not empty
    if (ctx.pushedNum != 0) {
      return true;
    }
    // emptyVersion has pushed
    return ctx.emptyVersion != ctx.pushedVersion;
  }

  public synchronized boolean isMarkedPushEmpty(String dataCenter) {
    final PushContext ctx = getPushContext(dataCenter);
    return ctx.emptyVersion != 0;
  }

  public synchronized boolean hasPushed() {
    if (CollectionUtils.isEmpty(lastPushContexts)) {
      return false;
    }
    for (PushContext ctx : lastPushContexts.values()) {
      if (ctx.pushedVersion != 0) {
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
    sb.append("sourceAddress=").append(getSourceAddress().buildAddressString()).append(", ");
    sb.append("acceptEncodes=");
    for (String encode : getAcceptEncodes()) {
      sb.append(encode).append(",");
    }
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
  protected synchronized String getOtherInfo() {
    return StringFormatter.format(
        "scope={},elementType={},multi={},ctx={}",
        scope,
        elementType,
        acceptMulti,
        lastPushContexts);
  }

  public synchronized String printPushContext() {
    return lastPushContexts == null ? "{}" : lastPushContexts.toString();
  }

  public synchronized long getPushedVersion(String dataCenter) {
    final PushContext ctx = getPushContext(dataCenter);
    return ctx.pushedVersion;
  }

  public synchronized int getPushedNum(String dataCenter) {
    final PushContext ctx = getPushContext(dataCenter);
    return ctx.pushedNum;
  }

  public synchronized long markPushEmpty(String dataCenter, long emptyVersion) {
    final PushContext ctx = getPushContext(dataCenter);

    if (ctx.emptyVersion != 0) {
      return ctx.emptyVersion;
    }
    ctx.emptyVersion = emptyVersion;
    return emptyVersion;
  }

  /** @return */
  @JsonIgnore
  public synchronized CircuitBreakerStatistic getStatistic() {
    return new CircuitBreakerStatistic(
        getGroup(),
        getSourceAddress().getIpAddress(),
        getSourceAddress().buildAddressString(),
        pushedFailCount,
        lastPushedFailTimeStamp);
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

    subscriber.setSourceAddress(URL.internURL(subscriber.getSourceAddress()));
    subscriber.setTargetAddress(URL.internURL(subscriber.getTargetAddress()));
    subscriber.setAttributes(subscriber.getAttributes());

    return subscriber;
  }

  protected Map<String, String> internAttributes(Map<String, String> attributes) {
    Map<String, String> intern = super.internAttributes(attributes);
    return com.alipay.sofa.registry.collections.Maps.trimMap(intern);
  }

  public String[] getAcceptEncodes() {
    if (acceptEncodes == null) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    return acceptEncodes;
  }

  public void internAcceptEncoding(String acceptEncoding) {
    if (StringUtils.isBlank(acceptEncoding)) {
      this.acceptEncodes = ArrayUtils.EMPTY_STRING_ARRAY;
      return;
    }
    String[] encodes = StringUtils.split(acceptEncoding, ',');
    for (int i = 0; i < encodes.length; i++) {
      encodes[i] = WordCache.getWordCache(encodes[i]);
    }
    this.acceptEncodes = encodes;
  }

  private static class PushContext {
    long pushedVersion;
    long lastMaxPushVersion = -1;
    long lastPushVersion = -1;
    long emptyVersion;
    int pushedNum = -1;

    @Override
    public String toString() {
      return StringFormatter.format(
          "PushCtx{pushedVer={},lastMaxPushVer={},num={},empty={}}",
          pushedVersion,
          lastMaxPushVersion,
          pushedNum,
          emptyVersion);
    }

    /**
     * Getter method for property <tt>pushedVersion</tt>.
     *
     * @return property value of pushedVersion
     */
    public long getPushedVersion() {
      return pushedVersion;
    }

    /**
     * Getter method for property <tt>lastMaxPushVersion</tt>.
     *
     * @return property value of lastMaxPushVersion
     */
    public long getLastMaxPushVersion() {
      return lastMaxPushVersion;
    }

    /**
     * Getter method for property <tt>lastPushVersion</tt>.
     *
     * @return property value of lastPushVersion
     */
    public long getLastPushVersion() {
      return lastPushVersion;
    }

    /**
     * Getter method for property <tt>emptyVersion</tt>.
     *
     * @return property value of emptyVersion
     */
    public long getEmptyVersion() {
      return emptyVersion;
    }

    /**
     * Getter method for property <tt>pushedNum</tt>.
     *
     * @return property value of pushedNum
     */
    public int getPushedNum() {
      return pushedNum;
    }
  }
}
