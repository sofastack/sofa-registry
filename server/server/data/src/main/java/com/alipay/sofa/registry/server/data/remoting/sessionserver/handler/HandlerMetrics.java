package com.alipay.sofa.registry.server.data.remoting.sessionserver.handler;

import io.prometheus.client.Counter;

public final class HandlerMetrics {
    private HandlerMetrics() {
    }

    static final class GetData {
        private static final Counter GET_DATUM_COUNTER     = Counter.build().namespace("data")
                                                               .subsystem("remote")
                                                               .name("getD_total")
                                                               .help("session get datum")
                                                               .labelNames("type").register();

        static final Counter.Child   GET_DATUM_Y_COUNTER   = GET_DATUM_COUNTER.labels("Y");
        static final Counter.Child   GET_DATUM_N_COUNTER   = GET_DATUM_COUNTER.labels("N");

        static final Counter         GET_PUBLISHER_COUNTER = Counter.build().namespace("data")
                                                               .subsystem("remote")
                                                               .name("getP_total")
                                                               .help("session get publisher")
                                                               .register();

    }

    static final class GetVersion {
        static final Counter GET_VERSION_COUNTER = Counter.build().namespace("data")
                                                     .subsystem("remote").name("getV_total")
                                                     .help("session get versions").register();
    }
}
