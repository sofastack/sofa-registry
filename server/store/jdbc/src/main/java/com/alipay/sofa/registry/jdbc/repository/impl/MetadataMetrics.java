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
package com.alipay.sofa.registry.jdbc.repository.impl;

import io.prometheus.client.Counter;

/**
 * @author xiaojian.xj
 * @version $Id: MetadataMetrics.java, v 0.1 2021年02月25日 16:34 xiaojian.xj Exp $
 */
public class MetadataMetrics {

  private MetadataMetrics() {}

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
                    .register();
    static final Counter.Child APPS_CACHE_HIT_COUNTER = FETCH_APPS_COUNTER.labels("Y");
    static final Counter.Child APPS_CACHE_MISS_COUNTER = FETCH_APPS_COUNTER.labels("N");

  }

  public static final class Register {

    public static final Counter REVISION_REGISTER_COUNTER =
        Counter.build()
            .namespace("metadata")
            .subsystem("revision")
            .name("revision_register_total")
            .help("revision register")
            .register();

    public static final Counter REVISION_HEARTBEAT_COUNTER =
            Counter.build()
                    .namespace("metadata")
                    .subsystem("revision")
                    .name("revision_heartbeat_total")
                    .help("revision register")
                    .register();

    public static final Counter REVISION_GC_COUNTER =
            Counter.build()
                    .namespace("metadata")
                    .subsystem("revision")
                    .name("revision_delete_total")
                    .help("revision delete")
                    .register();


    public static final Counter INTERFACE_APPS_REGISTER_COUNTER =
            Counter.build()
                    .namespace("metadata")
                    .subsystem("apps")
                    .name("apps_register_total")
                    .help("apps register")
                    .register();
  }
}
