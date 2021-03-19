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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import io.prometheus.client.Counter;

public final class HandlerMetrics {
  private HandlerMetrics() {}

  static final class GetData {
    private static final Counter GET_DATUM_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("remote")
            .name("getD_total")
            .help("session get datum")
            .labelNames("type")
            .register();

    static final Counter.Child GET_DATUM_Y_COUNTER = GET_DATUM_COUNTER.labels("Y");
    static final Counter.Child GET_DATUM_N_COUNTER = GET_DATUM_COUNTER.labels("N");

    static final Counter GET_PUBLISHER_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("remote")
            .name("getP_total")
            .help("session get publisher")
            .register();
  }

  static final class GetVersion {
    static final Counter GET_VERSION_COUNTER =
        Counter.build()
            .namespace("data")
            .subsystem("remote")
            .name("getV_total")
            .help("session get versions")
            .register();
  }
}
