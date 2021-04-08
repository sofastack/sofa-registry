package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.metaserver.nodes.MetaNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerBootstrap;
import com.alipay.sofa.registry.server.meta.metaserver.CurrentDcMetaServer;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.when;

public class HealthResourceTest {

  private Logger logger = LoggerFactory.getLogger(HealthResourceTest.class);

  private HealthResource healthResource;

  @Mock
  private MetaServerBootstrap metaServerBootstrap;

  @Mock
  private MetaLeaderService metaLeaderService;

  @Mock
  private CurrentDcMetaServer currentDcMetaServer;

  @Before
  public void beforeHealthResourceTest() {
    MockitoAnnotations.initMocks(this);
    healthResource = new HealthResource()
            .setCurrentDcMetaServer(currentDcMetaServer)
            .setMetaLeaderService(metaLeaderService)
            .setMetaServerBootstrap(metaServerBootstrap);
  }

  @Test
  public void testCheckHealth() {
    when(metaServerBootstrap.isRpcServerForSessionStarted()).thenReturn(false);
    when(metaServerBootstrap.isHttpServerStarted()).thenReturn(false);
    when(metaServerBootstrap.isRpcServerForMetaStarted()).thenReturn(false);
    Response response = healthResource.checkHealth();
    Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

    when(metaServerBootstrap.isRpcServerForSessionStarted()).thenReturn(true);
    when(metaServerBootstrap.isHttpServerStarted()).thenReturn(true);
    when(metaServerBootstrap.isRpcServerForMetaStarted()).thenReturn(true);
    when(metaServerBootstrap.isRpcServerForDataStarted()).thenReturn(true);
    when(currentDcMetaServer.getClusterMembers()).thenReturn(Lists.newArrayList(new MetaNode(new URL("127.0.0.1"), "dc")));

    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    response = healthResource.checkHealth();
    Assert.assertEquals(200, response.getStatus());
    logger.info("[testCheckHealth] {}", response);
  }
}