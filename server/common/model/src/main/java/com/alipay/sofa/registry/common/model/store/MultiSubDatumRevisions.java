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

import com.alipay.sofa.registry.cache.Sizer;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author huicha
 * @date 2025/3/13
 */
public class MultiSubDatumRevisions implements Serializable, Sizer {

  private static final long serialVersionUID = 8684268269248840832L;

  private static final MultiSubDatumRevisions INVALID_DATUM_REVISIONS =
      new MultiSubDatumRevisions("", Collections.emptyMap());

  private final String dataInfoId;

  private final Map<String /* data center */, SubDatumRevisions> datumRevisionMap;

  public static MultiSubDatumRevisions invalidDatumRevisions() {
    return INVALID_DATUM_REVISIONS;
  }

  public static boolean valid(MultiSubDatumRevisions datumRevisions) {
    if (null == datumRevisions) {
      return false;
    }
    return datumRevisions.valid();
  }

  public MultiSubDatumRevisions(
      String dataInfoId, Map<String, SubDatumRevisions> datumRevisionMap) {
    this.dataInfoId = dataInfoId;
    this.datumRevisionMap = datumRevisionMap;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public Map<String, SubDatumRevisions> getDatumRevisionMap() {
    return datumRevisionMap;
  }

  public SubDatumRevisions getDatumRevisions(String dataCenter) {
    if (null == this.datumRevisionMap || this.datumRevisionMap.isEmpty()) {
      return null;
    }
    return this.datumRevisionMap.get(dataCenter);
  }

  public Set<String> getDataCenters() {
    if (null == this.datumRevisionMap || this.datumRevisionMap.isEmpty()) {
      return Collections.emptySet();
    }
    return this.datumRevisionMap.keySet();
  }

  public MultiSubDatumRevisions intern() {
    Map<String /* data center */, SubDatumRevisions> datumRevisionMap;
    if (null == this.datumRevisionMap || this.datumRevisionMap.isEmpty()) {
      datumRevisionMap = Collections.emptyMap();
    } else {
      datumRevisionMap = new HashMap<>(this.datumRevisionMap.size());
      for (Map.Entry<String /* data center */, SubDatumRevisions> entry :
          this.datumRevisionMap.entrySet()) {
        String dataCenter = entry.getKey();
        SubDatumRevisions datumRevisions = entry.getValue();
        datumRevisionMap.put(WordCache.getWordCache(dataCenter), datumRevisions.intern());
      }
    }
    return new MultiSubDatumRevisions(WordCache.getWordCache(this.dataInfoId), datumRevisionMap);
  }

  public boolean valid() {
    if (StringUtils.isEmpty(this.dataInfoId)) {
      return false;
    }

    if (null == this.datumRevisionMap || this.datumRevisionMap.isEmpty()) {
      return false;
    }

    return true;
  }

  @Override
  public int size() {
    // todo(xidong.rxd):
    return 0;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
