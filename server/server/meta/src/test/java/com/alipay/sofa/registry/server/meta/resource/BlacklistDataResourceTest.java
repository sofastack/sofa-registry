package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BlacklistDataResourceTest extends AbstractMetaServerTestBase {

  private BlacklistDataResource resource;

  private ProvideDataRepository provideDataRepository = spy(new InMemoryProvideDataRepo());

  private DefaultProvideDataNotifier dataNotifier;

  @Before
  public void before() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    resource = new BlacklistDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataRepository(provideDataRepository);
  }

  @Test
  public void testBlacklistPush() {
    resource.blacklistPush("{\"FORBIDDEN_PUB\":{\"IP_FULL\":[\"1.1.1.1\",\"10.15.233.150\"]},\"FORBIDDEN_SUB_BY_PREFIX\":{\"IP_FULL\":[\"1.1.1.1\"]}}");
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    Assert.assertNotNull(provideDataRepository.get(ValueConstants.BLACK_LIST_DATA_ID));
  }

  @Test(expected = RuntimeException.class)
  public void testBlacklistPushWithException() {
    doThrow(new SofaRegistryRuntimeException("expected exception")).when(provideDataRepository).put(anyString(), anyString());
    resource.blacklistPush("{\"FORBIDDEN_PUB\":{\"IP_FULL\":[\"1.1.1.1\",\"10.15.233.150\"]},\"FORBIDDEN_SUB_BY_PREFIX\":{\"IP_FULL\":[\"1.1.1.1\"]}}");
  }
}