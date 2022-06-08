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
package com.alipay.sofa.registry.server.data.resource.config;

import com.alipay.sofa.registry.remoting.jersey.config.JerseyConfig;
import com.alipay.sofa.registry.remoting.jersey.swagger.SwaggerApiListingResource;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Swagger;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/** @Author dzdx @Date 2022/6/9 15:36 @Version 1.0 */
public class DataJerseyConfig extends JerseyConfig {
  @Autowired private DataServerConfig dataServerConfig;

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
    config.setSchemes(new String[] {"http", "https"});
    config.setResourcePackage(
        StringUtils.join(new String[] {"com.alipay.sofa.registry.server.data.resource"}, ","));

    config.setPrettyPrint(true);
    config.setScan(true);
    Swagger swagger = config.getSwagger();
    this.register(new SwaggerApiListingResource(swagger));
  }
}
