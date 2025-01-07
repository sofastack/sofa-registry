package com.alipay.sofa.registry.server.session.remoting.console.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.Node;
import com.alipay.sofa.registry.common.model.sessionserver.QueryPublisherRequest;
import com.alipay.sofa.registry.common.model.sessionserver.SimplePublisher;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author huicha
 * @date 2024/12/23
 */
public class QueryPublisherRequestHandlerTest {


  @Test
  public void testHandle() {
    String dataInfoId = "test-data-info-id";

    List<Publisher> mockPublishers = new ArrayList<>();
    for (int index = 0; index < 3; index++) {
      Publisher mockPublisher = new Publisher();
      mockPublisher.setDataInfoId(dataInfoId);
      mockPublisher.setClientId("ClientId-" + index);
      mockPublisher.setSourceAddress(URL.valueOf("127.0.0." + index + ":1234"));
      mockPublisher.setAppName("App");
      mockPublishers.add(mockPublisher);
    }

    DataStore dataStore = mock(DataStore.class);
    when(dataStore.getDatas(Mockito.eq(dataInfoId))).thenReturn(mockPublishers);

    QueryPublisherRequestHandler handler = new QueryPublisherRequestHandler();
    handler
            .setExecutorManager(new ExecutorManager(TestUtils.newSessionConfig("testDc")))
            .setSessionDataStore(dataStore);

    Assert.assertNotNull(handler.getExecutor());
    Assert.assertEquals(handler.interest(), QueryPublisherRequest.class);
    Assert.assertEquals(handler.getConnectNodeType(), Node.NodeType.CONSOLE);
    Assert.assertEquals(handler.getType(), ChannelHandler.HandlerType.PROCESSER);
    Assert.assertEquals(handler.getInvokeType(), ChannelHandler.InvokeType.SYNC);
    Assert.assertFalse(((CommonResponse) handler.buildFailedResponse("msg")).isSuccess());

    QueryPublisherRequest notExistReq = new QueryPublisherRequest("not-exist");
    GenericResponse<List<SimplePublisher>> notExistResp = (GenericResponse) handler.doHandle(null, notExistReq);
    Assert.assertTrue(notExistResp.isSuccess());
    List<SimplePublisher> notExistPublishers = notExistResp.getData();
    Assert.assertTrue(CollectionUtils.isEmpty(notExistPublishers));

    QueryPublisherRequest existReq = new QueryPublisherRequest(dataInfoId);
    GenericResponse<List<SimplePublisher>> existResp = (GenericResponse) handler.doHandle(null, existReq);
    Assert.assertTrue(existResp.isSuccess());
    List<SimplePublisher> existPublishers = existResp.getData();
    Assert.assertFalse(CollectionUtils.isEmpty(existPublishers));
    Assert.assertEquals(3, existPublishers.size());

    for (int index = 0; index < existPublishers.size(); index++) {
      SimplePublisher existPublisher = existPublishers.get(index);

      String clientId = existPublisher.getClientId();
      String sourceAddr = existPublisher.getSourceAddress();
      String appName = existPublisher.getAppName();

      Assert.assertEquals("ClientId-" + index, clientId);
      Assert.assertEquals("127.0.0." + index + ":1234", sourceAddr);
      Assert.assertEquals("App", appName);
    }
  }

}