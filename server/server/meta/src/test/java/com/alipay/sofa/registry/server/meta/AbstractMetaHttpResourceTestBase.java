package com.alipay.sofa.registry.server.meta;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Server;
import com.alipay.sofa.registry.remoting.jersey.exchange.JerseyExchange;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerConfiguration;
import com.google.common.collect.Maps;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chen.zhu
 * <p>
 * Apr 06, 2021
 */
public class AbstractMetaHttpResourceTestBase extends AbstractMetaServerTestBase {

  private ResourceConfig resourceConfig = new ResourceConfig();

  private final AtomicBoolean httpStart = new AtomicBoolean(false);

  private Server httpServer;

  @Autowired
  private JerseyExchange jerseyExchange;

  @Autowired
  private ApplicationContext applicationContext;

  @Before
  public void beforeAbstractMetaHttpResourceTestBase() {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(MetaServerConfiguration.class);
    builder.run();
    openHttpServer();
  }

  @Test
  public void test() throws IOException {
    waitForAnyKeyToExit();
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
