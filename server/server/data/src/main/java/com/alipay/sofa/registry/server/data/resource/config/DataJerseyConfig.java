package com.alipay.sofa.registry.server.data.resource.config;

import com.alipay.sofa.registry.remoting.jersey.config.JerseyConfig;
import com.alipay.sofa.registry.remoting.jersey.swagger.SwaggerApiListingResource;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

/**
 * @Author dzdx
 * @Date 2022/6/9 15:36
 * @Version 1.0
 */
public class DataJerseyConfig extends JerseyConfig {
    @Autowired
    private DataServerConfig dataServerConfig;

    @PostConstruct
    public void init() {
        if (dataServerConfig.isSwaggerEnabled()) {
            configureSwagger();
        }
    }

    private void configureSwagger() {
        BeanConfig config = new BeanConfig();
        config.setTitle("SOFARegistry data API Document");
        config.setVersion("v1");
        config.setContact("sofastack");
        config.setSchemes(new String[]{"http", "https"});
        config.setResourcePackage(StringUtils.join(new String[]{
                "com.alipay.sofa.registry.server.data.resource"

        }, ","));
        config.setPrettyPrint(true);
        config.setScan(true);
        Swagger swagger = config.getSwagger();
        this.register(new SwaggerApiListingResource(swagger));
    }
}
