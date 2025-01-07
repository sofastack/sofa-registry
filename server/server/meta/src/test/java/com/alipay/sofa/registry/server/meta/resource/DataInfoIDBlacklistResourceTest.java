package com.alipay.sofa.registry.server.meta.resource;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataNotifier;
import com.alipay.sofa.registry.server.meta.provide.data.ProvideDataService;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;
import com.alipay.sofa.registry.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * @author huicha
 * @date 2024/12/13
 */
public class DataInfoIDBlacklistResourceTest extends AbstractMetaServerTestBase {

  private ProvideDataService createProvideDataService() {
    return spy(new InMemoryProvideDataRepo());
  }

  private DataInfoIDBlacklistResource createDataIDBlacklistResource(ProvideDataService provideDataService) {
    ProvideDataNotifier provideDataNotifier = mock(ProvideDataNotifier.class);
    return new DataInfoIDBlacklistResource()
            .setProvideDataNotifier(provideDataNotifier)
            .setProvideDataService(provideDataService);
  }

  private DataInfoIDBlacklistResource createDataIDBlacklistResource(ProvideDataService provideDataService,
                                                                    ProvideDataNotifier provideDataNotifier) {
    return new DataInfoIDBlacklistResource()
            .setProvideDataNotifier(provideDataNotifier)
            .setProvideDataService(provideDataService);
  }

  @Test
  public void testAddAndDelete() {
    ProvideDataService provideDataService = createProvideDataService();
    DataInfoIDBlacklistResource resource = this.createDataIDBlacklistResource(provideDataService);

    String dataIdOne = "dataid.black.list.test";
    String group = "dataid-black-list-test-group";
    String instanceId = "DEFAULT_INSTANCE_ID";

    // 添加了两个数据
    Result resultOne = resource.addBlackList(dataIdOne, group, instanceId);
    Assert.assertTrue(resultOne.isSuccess());

    String dataIdTwo = "dataid.black.list.test2";
    Result resultTwo = resource.addBlackList(dataIdTwo, group, instanceId);
    Assert.assertTrue(resultTwo.isSuccess());

    // 因此这里的查询结果也应该是两条
    DBResponse<PersistenceData> queryResult = provideDataService.queryProvideData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID);
    Assert.assertEquals(OperationStatus.SUCCESS, queryResult.getOperationStatus());
    Assert.assertNotNull(queryResult.getEntity());
    PersistenceData persistenceData = queryResult.getEntity();
    String dataJson = persistenceData.getData();
    Set<String> data = JsonUtils.read(dataJson, new TypeReference<Set<String>>() {
    });
    Assert.assertEquals(2, data.size());
    Assert.assertTrue(data.contains(String.format("%s#@#%s#@#%s", dataIdOne, instanceId, group)));
    Assert.assertTrue(data.contains(String.format("%s#@#%s#@#%s", dataIdTwo, instanceId, group)));

    // 删除了第一条数据以及一条不存在的数据
    Result deleteResultOne = resource.deleteBlackList(dataIdOne, group, instanceId);
    Assert.assertTrue(deleteResultOne.isSuccess());

    String notExistDataId = "not.exist";
    Result deleteResultTwo = resource.deleteBlackList(notExistDataId, group, instanceId);
    Assert.assertTrue(deleteResultTwo.isSuccess());

    // 因此这里的查询结果应该是只有一条数据，且是第二条数据
    DBResponse<PersistenceData> queryResultTwo = provideDataService.queryProvideData(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID);
    Assert.assertEquals(OperationStatus.SUCCESS, queryResultTwo.getOperationStatus());
    Assert.assertNotNull(queryResultTwo.getEntity());
    PersistenceData persistenceDataTwo = queryResultTwo.getEntity();
    String dataJsonTwo = persistenceDataTwo.getData();
    Set<String> dataTwo = JsonUtils.read(dataJsonTwo, new TypeReference<Set<String>>() {
    });
    Assert.assertEquals(1, dataTwo.size());
    Assert.assertTrue(data.contains(String.format("%s#@#%s#@#%s", dataIdTwo, instanceId, group)));
  }

  @Test
  public void testNotify() {
    String dataId = "dataid.black.list.test";
    String group = "dataid-black-list-test-group";
    String instanceId = "DEFAULT_INSTANCE_ID";

    AtomicInteger counter = new AtomicInteger(0);

    ProvideDataService provideDataService = createProvideDataService();
    DataInfoIDBlacklistResource resource = this.createDataIDBlacklistResource(provideDataService, event -> {
      // 这个数据是提供给 Session 消费的，因此消费的节点类型有且只有 Session
      Set<NodeType> nodeTypes = event.getNodeTypes();
      Assert.assertEquals(1, nodeTypes.size());
      Assert.assertTrue(nodeTypes.contains(NodeType.SESSION));

      // 检查 DataInfoId 是否是预期的
      String dataInfoId = event.getDataInfoId();
      Assert.assertEquals(ValueConstants.SESSION_DATAID_BLACKLIST_DATA_ID, dataInfoId);

      // 增加计数
      counter.addAndGet(1);
    });

    Result result = resource.addBlackList(dataId, group, instanceId);
    Assert.assertTrue(result.isSuccess());
    Assert.assertEquals(1, counter.get());
  }
}