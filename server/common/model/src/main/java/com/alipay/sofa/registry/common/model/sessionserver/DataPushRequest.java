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

import com.alipay.sofa.registry.common.model.store.SubDatum;
import com.alipay.sofa.registry.util.StringFormatter;
import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: DataPushRequest.java, v 0.1 2018-08-29 18:11 shangyu.wh Exp $
 */
public class DataPushRequest implements Serializable {

  private final SubDatum datum;

  /**
   * constructor
   *
   * @param datum datum
   */
  public DataPushRequest(SubDatum datum) {
    this.datum = datum;
  }

  public SubDatum getDatum() {
    return datum;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "DataPushRequest{{},{},ver={},num={},bytes={}}",
        datum.getDataInfoId(),
        datum.getDataCenter(),
        datum.getVersion(),
        datum.getPubNum(),
        datum.getDataBoxBytes());
  }
}
