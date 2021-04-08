package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.exception.SofaRegistryRuntimeException;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.metaserver.impl.DefaultMetaServerManager;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetaDigestResourceTest {

  private MetaDigestResource metaDigestResource;

  private StopPushDataResource stopPushDataResource;

  private DefaultMetaServerManager metaServerManager;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataRepository provideDataRepository = spy(new AbstractMetaServerTestBase.InMemoryProvideDataRepo());

  @Before
  public void beforeMetaDigestResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    stopPushDataResource = new StopPushDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataRepository(provideDataRepository);
    metaDigestResource = new MetaDigestResource()
            .setProvideDataRepository(provideDataRepository)
            .setMetaServerManager(metaServerManager);
  }

  @Test(expected = RuntimeException.class)
  public void testGetRegisterNodeByType() {
    metaDigestResource.getRegisterNodeByType(Node.NodeType.META.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.META);
    metaDigestResource.getRegisterNodeByType(Node.NodeType.DATA.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.DATA);
    metaDigestResource.getRegisterNodeByType(Node.NodeType.SESSION.name());
    verify(metaServerManager, times(1)).getSummary(Node.NodeType.SESSION);
    when(metaServerManager.getSummary(any())).thenThrow(new SofaRegistryRuntimeException("expected exception"));
    metaDigestResource.getRegisterNodeByType(Node.NodeType.SESSION.name());
  }

  @Test
  public void testGetPushSwitch() {
    String key = "pushSwitch";
    String val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("open", val);

    DataInfo dataInfo = DataInfo.valueOf(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID);
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setDataId(dataInfo.getDataId());
    persistenceData.setGroup(dataInfo.getGroup());
    persistenceData.setInstanceId(dataInfo.getInstanceId());
    persistenceData.setVersion(System.currentTimeMillis());
    provideDataRepository.put(ValueConstants.STOP_PUSH_DATA_SWITCH_DATA_ID, JsonUtils.writeValueAsString(persistenceData));
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("open", val);

    stopPushDataResource.closePush();
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("closed", val);

    stopPushDataResource.openPush();
    val = metaDigestResource.getPushSwitch().get(key);
    Assert.assertEquals("open", val);
  }
}