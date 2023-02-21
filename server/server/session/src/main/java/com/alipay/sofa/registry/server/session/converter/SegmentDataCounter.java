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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.MultiSegmentData;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * @author xiaojian.xj
 * @version : SegmentDataCounter.java, v 0.1 2022年07月19日 10:42 xiaojian.xj Exp $
 */
public final class SegmentDataCounter {

  private final MultiSegmentData segmentData;

  private int dataCount;

  public SegmentDataCounter(MultiSegmentData segmentData) {
    this.segmentData = segmentData;
    this.dataCount = 0;
  }

  public void put(String zone, List<DataBox> datas) {
    if (datas == null) {
      datas = Lists.newArrayList();
    }
    this.segmentData.getUnzipData().put(zone, datas);
    this.dataCount += datas.size();

    segmentData.getDataCount().put(zone, datas.size());
  }

  /**
   * Getter method for property <tt>segmentData</tt>.
   *
   * @return property value of segmentData
   */
  public MultiSegmentData getSegmentData() {
    return segmentData;
  }

  /**
   * Getter method for property <tt>dataCount</tt>.
   *
   * @return property value of dataCount
   */
  public int getDataCount() {
    return dataCount;
  }
}
