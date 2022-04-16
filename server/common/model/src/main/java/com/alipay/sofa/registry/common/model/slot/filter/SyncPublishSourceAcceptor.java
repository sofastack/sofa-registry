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

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.constants.MultiValueConstants;
import com.google.common.base.Objects;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : SyncPublishSourceAcceptor.java, v 0.1 2022年05月14日 14:48 xiaojian.xj Exp $
 */
public class SyncPublishSourceAcceptor implements SyncSlotAcceptor {

  private static final String NAME = MultiValueConstants.SYNC_PUBLISH_SOURCE_ACCEPTOR;
  private final Set<PublishSource> filters;

  public SyncPublishSourceAcceptor(Set<PublishSource> filters) {
    this.filters = filters;
  }

  @Override
  public boolean accept(SyncAcceptorRequest request) {

    return true;
  }

  @Override
  public boolean filterOut(SyncAcceptorRequest request) {
    if (CollectionUtils.isEmpty(filters)) {
      return false;
    }

    return filters.contains(request.getDataInfoId());
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
    SyncPublishSourceAcceptor that = (SyncPublishSourceAcceptor) o;
    return Objects.equal(NAME, that.NAME);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(NAME);
  }
}
