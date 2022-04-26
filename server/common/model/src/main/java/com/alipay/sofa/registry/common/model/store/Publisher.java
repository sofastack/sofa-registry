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

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.PublishType;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

/**
 * @author shangyu.wh
 * @version $Id: Publisher.java, v 0.1 2017-11-30 16:04 shangyu.wh Exp $
 */
public class Publisher extends BaseInfo {

  /** UID */
  private static final long serialVersionUID = 5859214992132895021L;

  private List<ServerDataBox> dataList;

  private PublishType publishType = PublishType.NORMAL;

  private PublishSource publishSource = PublishSource.CLIENT;

  @JsonIgnore private ProcessId sessionProcessId;

  public Publisher() {}

  /**
   * Getter method for property <tt>sessionProcessId</tt>.
   *
   * @return property value of sessionProcessId
   */
  public ProcessId getSessionProcessId() {
    return sessionProcessId;
  }

  /**
   * Setter method for property <tt>sessionProcessId</tt>.
   *
   * @param sessionProcessId value to be assigned to property sessionProcessId
   */
  public void setSessionProcessId(ProcessId sessionProcessId) {
    this.sessionProcessId = sessionProcessId;
  }

  /**
   * Getter method for property <tt>dataList</tt>.
   *
   * @return property value of dataList
   */
  public List<ServerDataBox> getDataList() {
    return dataList;
  }

  /**
   * Setter method for property <tt>dataList</tt>.
   *
   * @param dataList value to be assigned to property dataList
   */
  public void setDataList(List<ServerDataBox> dataList) {
    this.dataList = dataList;
  }

  public PublishType getPublishType() {
    return publishType;
  }

  /**
   * Setter method for property <tt>publishType</tt>.
   *
   * @param publishType value to be assigned to property publishType
   */
  public void setPublishType(PublishType publishType) {
    this.publishType = publishType;
  }

  @Override
  @JsonIgnore
  public DataType getDataType() {
    return DataType.PUBLISHER;
  }

  @Override
  protected String getOtherInfo() {
    final StringBuilder sb = new StringBuilder("dataList=");
    if (dataList != null) {
      sb.append(dataList.size());
    } else {
      sb.append("null");
    }
    sb.append(",").append("publishType=").append(publishType);
    return sb.toString();
  }

  /**
   * change publisher word cache
   *
   * @param publisher
   * @return
   */
  public static Publisher internPublisher(Publisher publisher) {
    publisher.setRegisterId(publisher.getRegisterId());
    publisher.setDataInfoId(publisher.getDataInfoId());
    publisher.setInstanceId(publisher.getInstanceId());
    publisher.setGroup(publisher.getGroup());
    publisher.setDataId(publisher.getDataId());
    publisher.setClientId(publisher.getClientId());
    publisher.setCell(publisher.getCell());
    publisher.setProcessId(publisher.getProcessId());
    publisher.setAppName(publisher.getAppName());
    publisher.setSourceAddress(URL.internURL(publisher.getSourceAddress()));
    publisher.setTargetAddress(URL.internURL(publisher.getTargetAddress()));
    publisher.setAttributes(publisher.getAttributes());
    return publisher;
  }

  public static void internPublisher(List<Publisher> publishers) {
    for (Publisher p : publishers) {
      internPublisher(p);
    }
  }

  public PublishSource getPublishSource() {
    return publishSource;
  }

  public void setPublishSource(PublishSource publishSource) {
    this.publishSource = publishSource;
  }
}
