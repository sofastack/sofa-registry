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

import com.alipay.sofa.registry.common.model.constants.MultiValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import com.google.common.base.Objects;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : SyncPublisherGroupAcceptor.java, v 0.1 2022年07月22日 17:40 xiaojian.xj Exp $
 */
public class SyncPublisherGroupAcceptor implements SyncSlotAcceptor {

  private final String NAME = MultiValueConstants.SYNC_PUBLISHER_GROUP_ACCEPTOR;

  private final Set<String> acceptGroups;

  public SyncPublisherGroupAcceptor(Set<String> acceptGroups) {
    this.acceptGroups = acceptGroups;
  }

  @Override
  public boolean accept(SyncAcceptorRequest request) {
    ParaCheckUtil.checkNotNull(request, "SyncAcceptorRequest");
    if (CollectionUtils.isEmpty(acceptGroups)) {
      return false;
    }
    DataInfo dataInfo = DataInfo.valueOf(request.getDataInfoId());
    return acceptGroups.contains(dataInfo.getGroup());
  }

  @Override
  public boolean filterOut(SyncAcceptorRequest request) {
    return false;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SyncPublisherGroupAcceptor that = (SyncPublisherGroupAcceptor) o;
    return Objects.equal(NAME, that.NAME);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(NAME);
  }

  @Override
  public String toString() {
    return "SyncPublisherGroupAcceptor{" + ", acceptGroups=" + acceptGroups + '}';
  }
}
