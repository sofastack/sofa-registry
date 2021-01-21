package com.alipay.sofa.registry.server.data.change;

import io.prometheus.client.Counter;

public final class ChangeMetrics {
    private ChangeMetrics() {
    }

    private static final Counter CHANGE_COUNTER_            = Counter.build().namespace("data")
                                                                .subsystem("change")
                                                                .name("notify_total")
                                                                .help("notify session")
                                                                .labelNames("type").register();

    static final Counter.Child   CHANGE_COMMIT_COUNTER      = CHANGE_COUNTER_.labels("commit");
    // retry change
    static final Counter.Child   CHANGE_RETRY_COUNTER       = CHANGE_COUNTER_.labels("retry");
    // skip change
    static final Counter.Child   CHANGE_SKIP_COUNTER        = CHANGE_COUNTER_.labels("skip");

    static final Counter.Child   CHANGE_FAIL_COUNTER        = CHANGE_COUNTER_.labels("fail");
    static final Counter.Child   CHANGE_SUCCESS_COUNTER     = CHANGE_COUNTER_.labels("success");

    // should not use
    private static final Counter CHANGE_TEMP_COUNTER_       = Counter.build().namespace("data")
                                                                .subsystem("change")
                                                                .name("notify_temp_total")
                                                                .help("notify temp session")
                                                                .labelNames("type").register();

    static final Counter.Child   CHANGETEMP_SKIP_COUNTER    = CHANGE_TEMP_COUNTER_.labels("skip");
    static final Counter.Child   CHANGETEMP_COMMIT_COUNTER  = CHANGE_TEMP_COUNTER_.labels("commit");
    static final Counter.Child   CHANGETEMP_SUCCESS_COUNTER = CHANGE_TEMP_COUNTER_
                                                                .labels("success");
    static final Counter.Child   CHANGETEMP_FAIL_COUNTER    = CHANGE_TEMP_COUNTER_.labels("fail");
}
