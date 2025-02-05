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
package com.alipay.sofa.registry.server.data.multi.cluster.loggers;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import org.apache.logging.log4j.core.async.Hack;

/**
 * @author xiaojian.xj
 * @version : Loggers.java, v 0.1 2022年05月06日 20:40 xiaojian.xj Exp $
 */
public class Loggers {

  private Loggers() {}

  public static final Logger SYNC_SRV_LOGGER = LoggerFactory.getLogger("SYNC-SRV");

  public static final Logger MULTI_CLUSTER_SRV_LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-SRV");

  public static final Logger MULTI_CLUSTER_SYNC_DELTA_LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-SYNC", "[SyncDelta]");

  public static final Logger MULTI_CLUSTER_SYNC_ALL_LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-SYNC", "[SyncAll]");

  public static final Logger MULTI_CLUSTER_SYNC_DIGEST_LOGGER =
      LoggerFactory.getLogger("MULTI-CLUSTER-SYNC-DIGEST");

  public static final Logger MULTI_CLUSTER_SLOT_TABLE =
      LoggerFactory.getLogger("MULTI-CLUSTER-SLOT-TABLE");

  public static final Logger MULTI_PUT_LOGGER =
      Hack.hackLoggerDisruptor(LoggerFactory.getLogger("MULTI-PUT"));
}
