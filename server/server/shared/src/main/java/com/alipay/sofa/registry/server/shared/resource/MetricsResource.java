package com.alipay.sofa.registry.server.shared.resource;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.Writer;

@Path("metrics")
public class MetricsResource {

    @GET
    @Produces(TextFormat.CONTENT_TYPE_004)
    public Response metrics() {
        return Response
                .ok()
                .type(TextFormat.CONTENT_TYPE_004)
                .entity((StreamingOutput)
                        output -> {
                            try (final Writer writer = new OutputStreamWriter(output)) {
                                TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
                            }
                        })
                .build();
    }
}
