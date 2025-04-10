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

import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionKey;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author huicha
 * @date 2025/3/13
 */
public class SubDatumRevision implements Serializable {

  private static final long serialVersionUID = -7066583088251226438L;

  private final String dataCenter;

  private final String dataInfoId;

  private final DatumRevisionKey datumRevisionKey;

  // this field may be null
  private final SubDatumRevisionData datumRevisionData;

  public static SubDatumRevision fromMissData(
      String dataCenter, String dataInfoId, DatumRevisionKey datumRevisionKey) {
    return new SubDatumRevision(dataCenter, dataInfoId, datumRevisionKey, null);
  }

  public static SubDatumRevision from(
      String dataCenter,
      String dataInfoId,
      DatumRevisionKey datumRevisionKey,
      SubDatumRevisionData datumRevisionData) {
    return new SubDatumRevision(dataCenter, dataInfoId, datumRevisionKey, datumRevisionData);
  }

  public SubDatumRevision(
      String dataCenter,
      String dataInfoId,
      DatumRevisionKey datumRevisionKey,
      SubDatumRevisionData datumRevisionData) {
    ParaCheckUtil.checkNotNull(datumRevisionKey, "SubDatumRevision.datumRevisionKey");
    this.dataCenter = dataCenter;
    this.dataInfoId = dataInfoId;
    this.datumRevisionKey = datumRevisionKey;
    this.datumRevisionData = datumRevisionData;
  }

  public String getDataCenter() {
    return dataCenter;
  }

  public String getDataInfoId() {
    return dataInfoId;
  }

  public DatumRevisionKey getDatumRevisionKey() {
    return datumRevisionKey;
  }

  public SubDatumRevisionData getDatumRevisionData() {
    return datumRevisionData;
  }

  public SubDatumRevision intern() {
    return new SubDatumRevision(
        WordCache.getWordCache(this.dataCenter),
        WordCache.getWordCache(this.dataInfoId),
        this.datumRevisionKey.intern(),
        null == this.datumRevisionData ? null : this.datumRevisionData.intern());
  }

  public boolean missData() {
    return null == this.datumRevisionData;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
