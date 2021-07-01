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
package com.alipay.sofa.registry.server.data.change;

import com.alipay.sofa.registry.common.model.TraceTimes;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

/**
 * event for data changed
 *
 * @author qian.lqlq
 * @version $Id: DataChangeEvent.java, v 0.1 2017-12-07 18:44 qian.lqlq Exp $
 */
public class DataChangeEvent {
  private final String dataCenter;
  private final List<String> dataInfoIds;
  private final TraceTimes traceTimes;

  public DataChangeEvent(String dataCenter, List<String> dataInfoIds, TraceTimes parentTimes) {
    this.dataCenter = dataCenter;
    this.dataInfoIds = Collections.unmodifiableList(Lists.newArrayList(dataInfoIds));
    traceTimes = parentTimes.copy();
  }

  /**
   * Getter method for property <tt>dataCenter</tt>.
   *
   * @return property value of dataCenter
   */
  public String getDataCenter() {
    return dataCenter;
  }

  public List<String> getDataInfoIds() {
    return dataInfoIds;
  }

  public TraceTimes getTraceTimes() {
    return traceTimes;
  }

  @Override
  public String toString() {
    return "DataChangeEvent{"
        + "dataCenter='"
        + dataCenter
        + '\''
        + ", dataInfoIds='"
        + dataInfoIds
        + '\''
        + '}';
  }
}
