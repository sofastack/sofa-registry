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
package com.alipay.sofa.registry.server.shared.resource;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("metrics")
public class MetricsResource {

  @GET
  @Produces(TextFormat.CONTENT_TYPE_004)
  public Response metrics() {
    return Response.ok()
        .type(TextFormat.CONTENT_TYPE_004)
        .entity(
            (StreamingOutput)
                output -> {
                  try (final Writer writer = new OutputStreamWriter(output)) {
                    TextFormat.write004(
                        writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
                  }
                })
        .build();
  }
}
