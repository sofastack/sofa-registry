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

import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: FetchProvideDataRequest.java, v 0.1 2018-04-17 21:19 shangyu.wh Exp $
 */
public class FetchProvideDataRequest implements Serializable {

  private final String dataInfoId;

  /**
   * construtor
   *
   * @param dataInfoId dataInfoId
   */
  public FetchProvideDataRequest(String dataInfoId) {
    this.dataInfoId = dataInfoId;
  }

  /**
   * Getter method for property <tt>dataInfoId</tt>.
   *
   * @return property value of dataInfoId
   */
  public String getDataInfoId() {
    return dataInfoId;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FetchProvideDataRequest{");
    sb.append("dataInfoId='").append(dataInfoId).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
