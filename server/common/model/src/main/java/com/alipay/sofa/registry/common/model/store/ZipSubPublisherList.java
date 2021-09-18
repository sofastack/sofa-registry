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

import com.alipay.sofa.registry.compress.CompressedItem;
import java.io.Serializable;

public class ZipSubPublisherList extends CompressedItem implements Serializable {
  private static final long serialVersionUID = 7412542391154672610L;
  private final int pubNum;

  public ZipSubPublisherList(byte[] compressedData, int originSize, String encoding, int pubNum) {
    super(compressedData, originSize, encoding);
    this.pubNum = pubNum;
  }

  public int getPubNum() {
    return pubNum;
  }

  @Override
  public int size() {
    return super.size() + 4;
  }
}
