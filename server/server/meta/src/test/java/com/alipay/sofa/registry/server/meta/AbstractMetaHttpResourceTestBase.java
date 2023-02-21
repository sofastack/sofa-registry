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
package com.alipay.sofa.registry.server.meta;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.exchange.Exchange;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

/**
 * @author chen.zhu
 *     <p>Apr 06, 2021
 */
public class AbstractMetaHttpResourceTestBase extends AbstractMetaServerTestBase {

  private ResourceConfig resourceConfig = new ResourceConfig();

  private final AtomicBoolean httpStart = new AtomicBoolean(false);

  private Server httpServer;

  @Autowired private Exchange jerseyExchange;

  @Autowired private ApplicationContext applicationContext;

  @Before
  public void beforeAbstractMetaHttpResourceTestBase() {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(MetaServerConfiguration.class);
    builder.run();
    openHttpServer();
  }

  private void openHttpServer() {
    try {
      if (httpStart.compareAndSet(false, true)) {
        bindResourceConfig();
        httpServer =
            jerseyExchange.open(
                new URL(
                    NetUtil.getLocalAddress().getHostAddress(),
                    metaServerConfig.getHttpServerPort()),
                new ResourceConfig[] {resourceConfig});
        logger.info("Open http server port {} success!", metaServerConfig.getHttpServerPort());
      }
    } catch (Exception e) {
      httpStart.set(false);
      logger.error("Open http server port {} error!", metaServerConfig.getHttpServerPort(), e);
      throw new RuntimeException("Open http server error!", e);
    }
  }

  private void bindResourceConfig() {
    registerInstances(Path.class);
    registerInstances(Provider.class);
  }

  private void registerInstances(Class<? extends Annotation> annotationType) {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);
    if (beans != null && beans.size() > 0) {
      beans.forEach((beanName, bean) -> resourceConfig.registerInstances(bean));
    }
  }
}
