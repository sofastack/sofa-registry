package com.alipay.sofa.registry.server.meta.resource.filter;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.remoting.jersey.JerseyJettyServer;
import com.alipay.sofa.registry.server.meta.AbstractH2DbTestBase;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeaderAwareFilterTest extends AbstractH2DbTestBase {

  @Autowired
  private LeaderAwareFilter leaderAwareFilter;

  private MetaLeaderService metaLeaderService;

  @Before
  public void beforeLeaderAwareFilterTest() {
    metaLeaderService = mock(MetaLeaderService.class);
    leaderAwareFilter.setMetaLeaderService(metaLeaderService);
  }


  @Test
  public void testFilter() throws IOException, URISyntaxException {
    when(metaLeaderService.amILeader()).thenReturn(false).thenReturn(true);
    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    when(metaLeaderService.getLeaderEpoch()).thenReturn(DatumVersionUtil.nextId());
    Response response = JerseyClient.getInstance()
            .connect(new URL("127.0.0.1", 9615))
            .getWebTarget()
            .path("openapi/v1/slot/table/reconcile/status")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
    Assert.assertEquals(200, response.getStatus());
  }

}