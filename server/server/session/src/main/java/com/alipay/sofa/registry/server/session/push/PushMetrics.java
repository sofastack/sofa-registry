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
package com.alipay.sofa.registry.server.session.push;

import io.prometheus.client.Counter;

public final class PushMetrics {
    private PushMetrics() {
    }

    static final class Fetch {
        static final Counter         CHANGE_TASK_COUNTER   = Counter.build().namespace("session")
                                                               .subsystem("fetch")
                                                               .name("change_task_total")
                                                               .help("change task").register();
        static final Counter         REGISTER_TASK_COUNTER = Counter.build().namespace("session")
                                                               .subsystem("fetch")
                                                               .name("register_task_total")
                                                               .help("register task").register();
        static final Counter         PUSH_EMPTY_COUNTER    = Counter.build().namespace("session")
                                                               .subsystem("fetch")
                                                               .name("empty_task_total")
                                                               .help("empty task").register();
        static final Counter         PUSH_TEMP_COUNTER     = Counter.build().namespace("session")
                                                               .subsystem("fetch")
                                                               .name("temp_task_total")
                                                               .help("temp task").register();

        private static final Counter CACHE_COUNTER         = Counter.build().namespace("session")
                                                               .subsystem("fetch")
                                                               .name("cache_total").help(" cache")
                                                               .labelNames("hit").register();

        static final Counter.Child   CACHE_HIT_COUNTER     = CACHE_COUNTER.labels("Y");
        static final Counter.Child   CACHE_MISS_COUNTER    = CACHE_COUNTER.labels("N");
    }

    static final class Push {
        private static final Counter PENDING_COUNTER             = Counter.build()
                                                                     .namespace("session")
                                                                     .subsystem("push")
                                                                     .name("pending_total")
                                                                     .help("pending fetch")
                                                                     .labelNames("type").register();
        static final Counter.Child   PENDING_REPLACE_COUNTER     = PENDING_COUNTER
                                                                     .labels("replace");
        static final Counter.Child   PENDING_NEW_COUNTER         = PENDING_COUNTER.labels("new");
        static final Counter.Child   PENDING_SKIP_COUNTER        = PENDING_COUNTER.labels("skip");

        static final Counter         COMMIT_COUNTER              = Counter.build()
                                                                     .namespace("session")
                                                                     .subsystem("push")
                                                                     .name("fire_commit_total")
                                                                     .help("commit task")
                                                                     .register();
        private static final Counter PUSH_CLIENT_COUNTER         = Counter.build()
                                                                     .namespace("session")
                                                                     .subsystem("push")
                                                                     .name("push_client_total")
                                                                     .help("push client task")
                                                                     .labelNames("type").register();
        static final Counter.Child   PUSH_CLIENT_PUSHING_COUNTER = PUSH_CLIENT_COUNTER.labels("I");
        static final Counter.Child   PUSH_CLIENT_SUCCESS_COUNTER = PUSH_CLIENT_COUNTER.labels("Y");
        static final Counter.Child   PUSH_CLIENT_FAIL_COUNTER    = PUSH_CLIENT_COUNTER.labels("N");
    }

}
