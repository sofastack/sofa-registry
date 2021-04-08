package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.metaserver.Lease;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.lease.filter.DefaultRegistryBlacklistManager;
import com.alipay.sofa.registry.server.meta.lease.filter.RegistryBlacklistManager;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class RegistryCoreOpsResourceTest extends AbstractMetaServerTestBase {

  private RegistryBlacklistManager blacklistManager;

  private RegistryCoreOpsResource resource;

  private ProvideDataRepository repository = spy(new InMemoryProvideDataRepo());

  @Before
  public void before() {
    blacklistManager = new DefaultRegistryBlacklistManager(repository);
    resource = new RegistryCoreOpsResource().setRegistryBlacklistManager(blacklistManager);
  }
  @Test
  public void testKickoffServer() {
    CommonResponse response = resource.kickoffServer("fakeip");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("invalid ip address: fakeip", response.getMessage());

    response = resource.kickoffServer("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertFalse(blacklistManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));
  }

  @Test
  public void testKickoffServerException() {
    doThrow(new SofaRegistryRuntimeException("expected")).when(repository).put(anyString(), anyString());
    CommonResponse response = resource.kickoffServer("127.0.0.1");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("expected", response.getMessage());
  }

  @Test
  public void testRejoinServerGroup() {
    CommonResponse response = resource.rejoinServerGroup("fakeip");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("invalid ip address: fakeip", response.getMessage());

    Assert.assertTrue(blacklistManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));

    response = resource.kickoffServer("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertFalse(blacklistManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));

    response = resource.rejoinServerGroup("127.0.0.1");
    Assert.assertTrue(response.isSuccess());
    Assert.assertTrue(blacklistManager.allowSelect(new Lease<>(new SimpleNode("127.0.0.1"), 100)));

  }

  @Test
  public void testRejoinServerGroupException() {
    resource.kickoffServer("127.0.0.1");
    doThrow(new SofaRegistryRuntimeException("expected")).when(repository).put(anyString(), anyString());
    CommonResponse response = resource.rejoinServerGroup("127.0.0.1");
    Assert.assertFalse(response.isSuccess());
    Assert.assertEquals("expected", response.getMessage());
  }
}