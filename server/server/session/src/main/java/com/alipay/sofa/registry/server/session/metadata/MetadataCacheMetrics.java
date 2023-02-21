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
package com.alipay.sofa.registry.server.session.metadata;

import io.prometheus.client.Counter;

/**
 * @author xiaojian.xj
 * @version : MetadataCacheMetrics.java, v 0.1 2022年06月24日 19:44 xiaojian.xj Exp $
 */
public class MetadataCacheMetrics {

  static final class Fetch {
    static final Counter FETCH_REVISION_COUNTER =
        Counter.build()
            .namespace("metadata")
            .subsystem("revision")
            .name("fetch_revision_total")
            .help("fetch revision")
            .labelNames("hit")
            .register();
    static final Counter.Child REVISION_CACHE_HIT_COUNTER = FETCH_REVISION_COUNTER.labels("Y");
    static final Counter.Child REVISION_CACHE_MISS_COUNTER = FETCH_REVISION_COUNTER.labels("N");

    static final Counter FETCH_APPS_COUNTER =
        Counter.build()
            .namespace("metadata")
            .subsystem("apps")
            .name("fetch_apps_total")
            .help("query apps")
            .labelNames("hit")
            .register();
    static final Counter.Child APPS_CACHE_HIT_COUNTER = FETCH_APPS_COUNTER.labels("Y");
    static final Counter.Child APPS_CACHE_MISS_COUNTER = FETCH_APPS_COUNTER.labels("N");
  }
}
