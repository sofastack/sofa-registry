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
package com.alipay.sofa.registry.common.model.sessionserver;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: DataPushRequest.java, v 0.1 2018-08-29 18:11 shangyu.wh Exp $
 */
public class DataPushRequest implements Serializable {

  private Datum datum;

  /**
   * constructor
   *
   * @param datum
   */
  public DataPushRequest(Datum datum) {
    this.datum = datum;
  }

  /**
   * Getter method for property <tt>datum</tt>.
   *
   * @return property value of datum
   */
  public Datum getDatum() {
    return datum;
  }

  /**
   * Setter method for property <tt>datum</tt>.
   *
   * @param datum value to be assigned to property datum
   */
  public void setDatum(Datum datum) {
    this.datum = datum;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DataPushRequest{");
    sb.append("dataInfoId=").append(datum.getDataInfoId());
    sb.append(", dataCenter=").append(datum.getDataCenter());
    sb.append(", version=").append(datum.getVersion());
    sb.append(", pubsize=").append(datum.publisherSize());
    sb.append('}');
    return sb.toString();
  }
}
