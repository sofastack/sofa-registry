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
package com.alipay.sofa.registry.server.session.strategy.impl;

import com.alipay.sofa.registry.common.model.InterestGroup;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

public final class Metrics {
  private Metrics() {}

  public static final class Access {

    private static final Counter PUB_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("access")
            .name("pub_total")
            .labelNames("type")
            .help("publisher access num")
            .register();
    private static final Counter.Child PUB_COUNTER_Y = PUB_COUNTER.labels("Y");
    private static final Counter.Child PUB_COUNTER_N = PUB_COUNTER.labels("N");

    private static final Counter SUB_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("access")
            .name("sub_total")
            .labelNames("type")
            .help("subscriber access num")
            .register();

    private static final Counter.Child SUB_COUNTER_Y = SUB_COUNTER.labels("Y");
    private static final Counter.Child SUB_COUNTER_N = SUB_COUNTER.labels("N");

    private static final Counter WAT_COUNTER =
        Counter.build()
            .namespace("session")
            .subsystem("access")
            .name("wat_total")
            .labelNames("type")
            .help("watcher access num")
            .register();

    private static final Counter.Child WAT_COUNTER_Y = WAT_COUNTER.labels("Y");
    private static final Counter.Child WAT_COUNTER_N = WAT_COUNTER.labels("N");

    private static final Histogram PUB_SIZE =
        Histogram.build()
            .exponentialBuckets(10, 2, 15)
            .namespace("session")
            .subsystem("access")
            .name("pub_size")
            .labelNames("client", "group")
            .help("publisher data size")
            .register();

    static void pubCount(boolean success) {
      if (success) {
        PUB_COUNTER_Y.inc();
      } else {
        PUB_COUNTER_N.inc();
      }
    }

    static void subCount(boolean success) {
      if (success) {
        SUB_COUNTER_Y.inc();
      } else {
        SUB_COUNTER_N.inc();
      }
    }

    static void watCount(boolean success) {
      if (success) {
        WAT_COUNTER_Y.inc();
      } else {
        WAT_COUNTER_N.inc();
      }
    }

    public static void pubSize(String client, String group, double size) {
      PUB_SIZE.labels(client, InterestGroup.normalizeGroup(group)).observe(size);
    }
  }
}
