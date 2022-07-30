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
package com.alipay.sofa.registry.remoting.jersey.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.listing.BaseApiListingResource;
import io.swagger.models.Swagger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/** @Author dzdx @Date 2022/6/9 17:48 @Version 1.0 */
@Path("/swagger.json")
public class SwaggerApiListingResource extends BaseApiListingResource {
  private final Swagger swagger;

  public SwaggerApiListingResource(Swagger swagger) {
    this.swagger = swagger;
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @ApiOperation(value = "The swagger definition in either JSON or YAML", hidden = true)
  public Response getListing() {
    return Response.ok().entity(swagger).build();
  }
}
