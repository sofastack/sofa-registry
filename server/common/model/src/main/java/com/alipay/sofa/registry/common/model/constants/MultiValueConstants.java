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
package com.alipay.sofa.registry.common.model.constants;

import com.alipay.sofa.registry.common.model.PublishSource;
import com.alipay.sofa.registry.common.model.slot.filter.SyncPublishSourceAcceptor;
import com.alipay.sofa.registry.common.model.slot.filter.SyncSlotAcceptor;
import com.google.common.collect.Sets;

/**
 * @author xiaojian.xj
 * @version : MultiValueConstants.java, v 0.1 2022年07月20日 14:41 xiaojian.xj Exp $
 */
public final class MultiValueConstants {

  private MultiValueConstants() {}

  public static final String SYNC_ACCEPT_ALL = "ACCEPT_ALL";

  public static final SyncSlotAcceptor DATUM_SYNCER_SOURCE_FILTER =
      new SyncPublishSourceAcceptor(Sets.newHashSet(PublishSource.DATUM_SYNCER));

  public static final String SYNC_SLOT_DATAINFOID_ACCEPTOR = "SyncSlotDataInfoIdAcceptor";

  public static final String SYNC_PUBLISH_SOURCE_ACCEPTOR = "SyncPublishSourceAcceptor";

  public static final String SYNC_PUBLISHER_GROUP_ACCEPTOR = "SyncPublisherGroupAcceptor";
}
