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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

/**
 * @author shangyu.wh
 * @version $Id: ProvideData.java, v 0.1 2018-04-17 20:13 shangyu.wh Exp $
 */
public class ProvideData implements Serializable {

  private final ServerDataBox provideData;

  private final String dataInfoId;

  private final Long version;

  /**
   * construtor
   *
   * @param provideData
   * @param dataInfoId
   * @param version
   */
  public ProvideData(ServerDataBox provideData, String dataInfoId, Long version) {
    this.provideData = provideData;
    this.dataInfoId = dataInfoId;
    this.version = version;
  }

  /**
   * Getter method for property <tt>provideData</tt>.
   *
   * @return property value of provideData
   */
  public ServerDataBox getProvideData() {
    return provideData;
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
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public Long getVersion() {
    return version;
  }

  public static String toString(ProvideData provideData) {
    if (provideData == null || provideData.getProvideData() == null) {
      return null;
    }
    return (String) provideData.getProvideData().getObject();
  }

  public static Boolean toBool(ProvideData provideData) {
    String obj = ProvideData.toString(provideData);
    Assert.isTrue(
        StringUtils.equals("true", obj) || StringUtils.equals("false", obj),
        String.format("provideDataKey: %s, value: %s toBool error.", provideData.dataInfoId, obj));
    return obj != null ? Boolean.parseBoolean(obj) : null;
  }

  public static Integer toInteger(ProvideData provideData) {
    String obj = ProvideData.toString(provideData);
    return obj != null ? Integer.valueOf(obj) : null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ProvideData{");
    sb.append("provideData=").append(provideData);
    sb.append(", dataInfoId='").append(dataInfoId).append('\'');
    sb.append(", version=").append(version);
    sb.append('}');
    return sb.toString();
  }
}
