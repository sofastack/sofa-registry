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
package com.alipay.sofa.registry.common.model.slot.filter;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.util.ParaCheckUtil;

/**
 * @author xiaojian.xj
 * @version : SyncAcceptorRequest.java, v 0.1 2022年07月20日 17:33 xiaojian.xj Exp $
 */
public class SyncAcceptorRequest {

  private final String dataInfoId;

  private final PublishSource source;

  private SyncAcceptorRequest(String dataInfoId, PublishSource source) {
    this.dataInfoId = dataInfoId;
    this.source = source;
  }

  public static SyncAcceptorRequest buildRequest(String dataInfoId) {
    ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
    return new SyncAcceptorRequest(dataInfoId, null);
  }

  public static SyncAcceptorRequest buildRequest(String dataInfoId, PublishSource source) {
    ParaCheckUtil.checkNotNull(dataInfoId, "dataInfoId");
    ParaCheckUtil.checkNotNull(source, "publishSource");
    return new SyncAcceptorRequest(dataInfoId, source);
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
   * Getter method for property <tt>source</tt>.
   *
   * @return property value of source
   */
  public PublishSource getSource() {
    return source;
  }
}
