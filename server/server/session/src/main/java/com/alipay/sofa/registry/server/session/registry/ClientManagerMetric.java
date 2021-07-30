/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.sofa.registry.server.session.registry;

import com.alipay.sofa.registry.metrics.GaugeFunc;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;

/**
 *
 * @author xiaojian.xj
 * @version : ClientManagerMetric.java, v 0.1 2021年07月31日 22:57 xiaojian.xj Exp $
 */
public class ClientManagerMetric {

    private static final Counter CLIENT_MANAGER_COUNTER =
            Counter.build()
                    .namespace("session")
                    .subsystem("client_manager")
                    .name("total")
                    .help(" total count")
                    .labelNames("operate")
                    .register();

    public static final Counter.Child CLIENT_OFF_COUNTER = CLIENT_MANAGER_COUNTER.labels("clientOff");
    public static final Counter.Child CLIENT_OPEN_COUNTER = CLIENT_MANAGER_COUNTER.labels("clientOpen");

    public static final GaugeFunc CLIENT_OFF_GAUGE =
            GaugeFunc.build()
                    .namespace("session")
                    .subsystem("client_off")
                    .name("address_total")
                    .help("client off address total")
                    .register();

    public static final Histogram ADDRESS_LOAD_DELAY_HISTOGRAM =
            Histogram.build()
                    .linearBuckets(0, 500, 30)
                    .namespace("session")
                    .subsystem("client_off")
                    .name("load_delay")
                    .help("address load delay")
                    .register();

}