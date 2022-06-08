package com.alipay.sofa.registry.remoting.jersey.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.jaxrs.listing.BaseApiListingResource;
import io.swagger.models.Swagger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * @Author dzdx
 * @Date 2022/6/9 17:48
 * @Version 1.0
 */


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