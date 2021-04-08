package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class SlotSyncResourceTest extends AbstractMetaServerTestBase {

  private SlotSyncResource slotSyncResource;

  private ProvideDataRepository repository = spy(new InMemoryProvideDataRepo());

  @Before
  public void before() {
    slotSyncResource = new SlotSyncResource().setProvideDataRepository(repository);
  }

  @Test
  public void testGetSlotSync() throws Exception {
    Map<String, Object> result = slotSyncResource.getSlotSync();
    Assert.assertEquals("null", result.get("syncSessionIntervalSec"));
    Assert.assertEquals("null", result.get("dataDatumExpire"));
  }
}