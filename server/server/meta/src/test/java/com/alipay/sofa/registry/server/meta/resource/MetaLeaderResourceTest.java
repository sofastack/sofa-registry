package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.alipay.sofa.registry.store.api.elector.LeaderElector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class MetaLeaderResourceTest {

  private MetaLeaderResource resource;

  @Mock
  private MetaLeaderService metaLeaderService;

  @Mock
  private LeaderElector leaderElector;

  @Before
  public void beforeMetaLeaderResourceTest() {
    MockitoAnnotations.initMocks(this);
    resource = new MetaLeaderResource().setLeaderElector(leaderElector).setMetaLeaderService(metaLeaderService);
  }

  @Test
  public void testQueryLeader() {
    when(metaLeaderService.getLeader()).thenReturn("127.0.0.1");
    GenericResponse<LeaderInfo> response = resource.queryLeader();
    Assert.assertTrue(response.isSuccess());
    Assert.assertEquals("127.0.0.1", response.getData().getLeader());

    when(metaLeaderService.getLeader()).thenThrow(new SofaRegistryRuntimeException("expected exception"));
    response = resource.queryLeader();
    Assert.assertFalse(response.isSuccess());

  }

  @Test
  public void testQuitLeader() {
    CommonResponse response = resource.quitElection();
    Assert.assertTrue(response.isSuccess());
    verify(leaderElector, times(1)).change2Observer();

    doThrow(new SofaRegistryRuntimeException("expected exception")).when(leaderElector).change2Observer();
    response = resource.quitElection();
    Assert.assertFalse(response.isSuccess());
  }
}