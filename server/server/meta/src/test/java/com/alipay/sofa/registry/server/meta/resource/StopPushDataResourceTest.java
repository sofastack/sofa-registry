package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StopPushDataResourceTest {

  private StopPushDataResource stopPushDataResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataRepository provideDataRepository = spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void beforeStopPushDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    stopPushDataResource = new StopPushDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataRepository(provideDataRepository);
  }

  @Test
  public void testClosePush() {
    stopPushDataResource.closePush();
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse dbResponse = provideDataRepository
            .get(DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getDataInfoId());
    Assert.assertEquals(OperationStatus.SUCCESS, dbResponse.getOperationStatus());
    PersistenceData persistenceData = JsonUtils.read((String)dbResponse.getEntity(), PersistenceData.class);
    Assert.assertTrue(Boolean.parseBoolean(persistenceData.getData()));
  }

  @Test
  public void testOpenPush() {
    stopPushDataResource.openPush();
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse dbResponse = provideDataRepository
            .get(DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID).getDataInfoId());
    Assert.assertEquals(OperationStatus.SUCCESS, dbResponse.getOperationStatus());
    PersistenceData persistenceData = JsonUtils.read((String)dbResponse.getEntity(), PersistenceData.class);
    Assert.assertFalse(Boolean.parseBoolean(persistenceData.getData()));
  }

  @Test
  public void testGetNodeTypes() {
    Assert.assertEquals(Sets.newHashSet(Node.NodeType.SESSION), stopPushDataResource.getNodeTypes());
  }
}