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

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.RegisterVersion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.CollectionUtils;

/**
 * @author shangyu.wh
 * @version $Id: BaseInfo.java, v 0.1 2017-11-30 16:31 shangyu.wh Exp $
 */
public abstract class BaseInfo implements Serializable, StoreData<String> {

  private static final long serialVersionUID = -6263388188316303789L;

  private String dataInfoId;

  private String dataId;

  private String clientId;

  private String instanceId;

  private String cell;

  private String appName;

  private String processId;

  private String registerId;

  private long version;

  private URL sourceAddress;

  private URL targetAddress;

  private ClientVersion clientVersion;

  private String group;

  private long registerTimestamp;

  private long clientRegisterTimestamp;

  private volatile Map<String, String> attributes;

  /** ClientVersion Enum */
  public enum ClientVersion {
    /** */
    ProtocolPackage("1.x"),
    /** */
    NProtocolpackage("2.x"),
    /** */
    MProtocolpackage("3.x"),
    /** */
    StoreData("4.x");

    private String version;

    /**
     * Set version val
     *
     * @param version
     */
    ClientVersion(String version) {
      this.version = version;
    }
  }

  /**
   * Getter method for property <tt>cell</tt>.
   *
   * @return property value of cell
   */
  public String getCell() {
    return cell;
  }

  /**
   * Setter method for property <tt>cell</tt>.
   *
   * @param cell value to be assigned to property cell
   */
  public void setCell(String cell) {
    this.cell = WordCache.getWordCache(cell);
  }

  /**
   * Getter method for property <tt>appName</tt>.
   *
   * @return property value of appName
   */
  public String getAppName() {
    return appName;
  }

  /**
   * Setter method for property <tt>appName</tt>.
   *
   * @param appName value to be assigned to property appName
   */
  public void setAppName(String appName) {
    this.appName = WordCache.getWordCache(appName);
  }

  /**
   * Getter method for property <tt>processId</tt>.
   *
   * @return property value of processId
   */
  public String getProcessId() {
    return processId;
  }

  /**
   * Setter method for property <tt>processId</tt>.
   *
   * @param processId value to be assigned to property processId
   */
  public void setProcessId(String processId) {
    this.processId = WordCache.getWordCache(processId);
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Setter method for property <tt>version</tt>.
   *
   * @param version value to be assigned to property version
   */
  public void setVersion(long version) {
    this.version = version;
  }

  /**
   * Getter method for property <tt>sourceAddress</tt>.
   *
   * @return property value of sourceAddress
   */
  public URL getSourceAddress() {
    return sourceAddress;
  }

  /**
   * Setter method for property <tt>sourceAddress</tt>.
   *
   * @param sourceAddress value to be assigned to property sourceAddress
   */
  public void setSourceAddress(URL sourceAddress) {
    this.sourceAddress = sourceAddress;
  }

  /**
   * Getter method for property <tt>targetAddress</tt>.
   *
   * @return property value of targetAddress
   */
  public URL getTargetAddress() {
    return targetAddress;
  }

  /**
   * Setter method for property <tt>targetAddress</tt>.
   *
   * @param targetAddress value to be assigned to property targetAddress
   */
  public void setTargetAddress(URL targetAddress) {
    this.targetAddress = targetAddress;
  }

  /**
   * Getter method for property <tt>attributes</tt>.
   *
   * @return property value of attributes
   */
  public Map<String, String> getAttributes() {
    Map<String, String> attrs = this.attributes;
    if (attrs == null) {
      return Collections.emptyMap();
    }
    return new HashMap<>(attrs);
  }

  public String attributeOf(String key) {
    Map<String, String> attrs = this.attributes;
    if (attrs == null) {
      return null;
    }
    return attrs.get(key);
  }

  /**
   * Setter method for property <tt>attributes</tt>.
   *
   * @param attributes value to be assigned to property attributes
   */
  public synchronized void setAttributes(Map<String, String> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      this.attributes = null;
    } else {
      this.attributes = internAttributes(attributes);
    }
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  /**
   * Setter method for property <tt>dataInfoId</tt>.
   *
   * @param dataInfoId value to be assigned to property dataInfoId
   */
  public void setDataInfoId(String dataInfoId) {
    this.dataInfoId = WordCache.getWordCache(dataInfoId);
  }

  @Override
  @JsonIgnore
  public String getId() {
    return registerId;
  }

  /**
   * Getter method for property <tt>registerId</tt>.
   *
   * @return property value of registerId
   */
  public String getRegisterId() {
    return registerId;
  }

  /**
   * Setter method for property <tt>registerId</tt>.
   *
   * @param registerId value to be assigned to property registerId
   */
  public void setRegisterId(String registerId) {
    this.registerId = registerId;
  }

  /**
   * Getter method for property <tt>dataId</tt>.
   *
   * @return property value of dataId
   */
  public String getDataId() {
    return dataId;
  }

  /**
   * Setter method for property <tt>dataId</tt>.
   *
   * @param dataId value to be assigned to property dataId
   */
  public void setDataId(String dataId) {
    this.dataId = WordCache.getWordCache(dataId);
  }

  /**
   * Getter method for property <tt>clientId</tt>.
   *
   * @return property value of clientId
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Setter method for property <tt>clientId</tt>.
   *
   * @param clientId value to be assigned to property clientId
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Getter method for property <tt>instanceId</tt>.
   *
   * @return property value of instanceId
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Setter method for property <tt>instanceId</tt>.
   *
   * @param instanceId value to be assigned to property instanceId
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = WordCache.getWordCache(instanceId);
  }

  /**
   * Getter method for property <tt>group</tt>.
   *
   * @return property value of group
   */
  public String getGroup() {
    return group;
  }

  /**
   * Setter method for property <tt>group</tt>.
   *
   * @param group value to be assigned to property group
   */
  public void setGroup(String group) {
    this.group = WordCache.getWordCache(group);
  }

  /**
   * Getter method for property <tt>registerTimestamp</tt>.
   *
   * @return property value of registerTimestamp
   */
  public long getRegisterTimestamp() {
    return registerTimestamp;
  }

  /**
   * Setter method for property <tt>registerTimestamp</tt>.
   *
   * @param registerTimestamp value to be assigned to property registerTimestamp
   */
  public void setRegisterTimestamp(long registerTimestamp) {
    this.registerTimestamp = registerTimestamp;
  }

  /**
   * Getter method for property <tt>clientVersion</tt>.
   *
   * @return property value of clientVersion
   */
  public ClientVersion getClientVersion() {
    return clientVersion;
  }

  /**
   * Setter method for property <tt>clientVersion</tt>.
   *
   * @param clientVersion value to be assigned to property clientVersion
   */
  public void setClientVersion(ClientVersion clientVersion) {
    this.clientVersion = clientVersion;
  }

  public long getClientRegisterTimestamp() {
    return clientRegisterTimestamp;
  }

  /**
   * Setter method for property <tt>clientRegisterTimestamp</tt>.
   *
   * @param clientRegisterTimestamp value to be assigned to property clientRegisterTimestamp
   */
  public void setClientRegisterTimestamp(long clientRegisterTimestamp) {
    this.clientRegisterTimestamp = clientRegisterTimestamp;
  }

  protected String getOtherInfo() {
    return "";
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(getDataType().toString());
    sb.append("{dataInfoId='").append(dataInfoId).append('\'');
    sb.append(", clientId='").append(clientId).append('\'');
    sb.append(", cell='").append(cell).append('\'');
    sb.append(", appName='").append(appName).append('\'');
    sb.append(", processId='").append(processId).append('\'');
    sb.append(", registerId='").append(registerId).append('\'');
    sb.append(", version=").append(version);
    sb.append(", sourceAddress=").append(sourceAddress);
    sb.append(", clientVersion=").append(clientVersion);
    sb.append(", registerTimestamp=").append(registerTimestamp);
    sb.append(", clientRegisterTimestamp=").append(clientRegisterTimestamp);
    sb.append(", otherInfo=").append(getOtherInfo());
    sb.append(", attributes=").append(attributes);
    sb.append('}');
    return sb.toString();
  }

  public ConnectId connectId() {
    return new ConnectId(
        getSourceAddress().getIpAddress(),
        getSourceAddress().getPort(),
        getTargetAddress().getIpAddress(),
        getTargetAddress().getPort());
  }

  public int attributesSize() {
    final Map<String, String> map = this.attributes;
    return map == null ? 0 : map.size();
  }

  protected Map<String, String> internAttributes(Map<String, String> attributes) {
    if (CollectionUtils.isEmpty(attributes)) {
      return Collections.emptyMap();
    }
    Map<String, String> ret = Maps.newHashMapWithExpectedSize(attributes.size());
    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      ret.put(WordCache.getWordCache(entry.getKey()), entry.getValue());
    }
    return ret;
  }

  public RegisterVersion registerVersion() {
    return RegisterVersion.of(getVersion(), getRegisterTimestamp());
  }
}
