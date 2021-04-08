package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.jdbc.domain.ProvideDataDomain;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.DefaultProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataNotifier;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.util.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author zhuchen
 * @date Apr 6, 2021, 7:45:13 PM
 */
public class ProvideDataResourceTest extends AbstractMetaServerTestBase {

  private ProvideDataResource provideDataResource;

  private DefaultProvideDataNotifier dataNotifier;

  private ProvideDataRepository provideDataRepository = spy(new InMemoryProvideDataRepo());

  @Before
  public void beforeProvideDataResourceTest() {
    dataNotifier = mock(DefaultProvideDataNotifier.class);
    provideDataResource = new ProvideDataResource()
            .setProvideDataNotifier(dataNotifier)
            .setProvideDataRepository(provideDataRepository);
  }

  @Test
  public void testPut() {
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);
    String dataInfoId =
            DataInfo.toDataInfoId(persistenceData.getDataId(), persistenceData.getInstanceId(), persistenceData.getGroup());
    verify(dataNotifier, times(1)).notifyProvideDataChange(any());
    DBResponse response = provideDataRepository.get(dataInfoId);
    Assert.assertEquals(OperationStatus.SUCCESS, response.getOperationStatus());
    PersistenceData read = JsonUtils.read((String)response.getEntity(), PersistenceData.class);
    Assert.assertEquals(persistenceData, read);
  }

  @Test
  public void testRemove() {
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);
    String dataInfoId =
            DataInfo.toDataInfoId(persistenceData.getDataId(), persistenceData.getInstanceId(), persistenceData.getGroup());
    DBResponse response = provideDataRepository.get(dataInfoId);
    Assert.assertEquals(OperationStatus.SUCCESS, response.getOperationStatus());

    PersistenceData other = new PersistenceData();
    other.setData(persistenceData.getData());
    other.setDataId(persistenceData.getDataId());
    other.setGroup(persistenceData.getGroup());
    other.setInstanceId(persistenceData.getInstanceId());
    other.setVersion(persistenceData.getVersion());
    provideDataResource.remove(other);
    response = provideDataRepository.get(dataInfoId);
    verify(dataNotifier, atLeast(1)).notifyProvideDataChange(any());
    Assert.assertEquals(OperationStatus.NOTFOUND, response.getOperationStatus());
  }

  @Test(expected = RuntimeException.class)
  public void testWhenProvideDataAccessFail() {
    when(provideDataRepository.put(anyString(), anyString())).thenThrow(new TimeoutException("expected exception"));
    PersistenceData persistenceData = new PersistenceData();
    persistenceData.setData("SampleWord");
    persistenceData.setDataId("dataId");
    persistenceData.setGroup("group");
    persistenceData.setInstanceId("InstanceId");
    persistenceData.setVersion(1000L);
    provideDataResource.put(persistenceData);

  }
}