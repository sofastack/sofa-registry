package com.alipay.sofa.registry.remoting.jersey.config;

import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * @Author dzdx
 * @Date 2022/6/8 14:13
 * @Version 1.0
 */

public abstract class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        this.register(JacksonFeature.class);
        this.register(SwaggerSerializers.class);
    }
}

