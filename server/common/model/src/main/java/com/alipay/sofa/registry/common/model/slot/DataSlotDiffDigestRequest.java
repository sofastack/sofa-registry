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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-05 14:24 yuzhi.lyz Exp $
 */
public class DataSlotDiffDigestRequest implements Serializable {
  private final long slotTableEpoch;
  // all dataInfoIds, diff by digest
  private final Map<String, DatumDigest> datumDigest;
  private final int slotId;

  public DataSlotDiffDigestRequest(
      long slotTableEpoch, int slotId, Map<String, DatumDigest> datumDigest) {
    this.slotTableEpoch = slotTableEpoch;
    this.slotId = slotId;
    this.datumDigest = datumDigest == null ? Collections.emptyMap() : datumDigest;
  }

  /**
   * Getter method for property <tt>slotId</tt>.
   *
   * @return property value of slotId
   */
  public int getSlotId() {
    return slotId;
  }

  /**
   * Getter method for property <tt>slotTableEpoch</tt>.
   *
   * @return property value of slotTableEpoch
   */
  public long getSlotTableEpoch() {
    return slotTableEpoch;
  }

  public Map<String, DatumDigest> getDatumDigest() {
    return Collections.unmodifiableMap(datumDigest);
  }

  @Override
  public String toString() {
    return "DiffDigest{"
        + "slotId="
        + slotId
        + ", epoch="
        + slotTableEpoch
        + ", digests="
        + datumDigest.size()
        + '}';
  }
}
