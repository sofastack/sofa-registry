package com.alipay.sofa.registry.remoting.exchange.message;

import com.alipay.sofa.registry.common.model.store.URL;
import org.junit.Assert;
import org.junit.Test;

public class SyncRequestTest {
  @Test
  public void test() {
    Object obj = new Object();
    URL url = new URL("192.168.1.1", 8888);
    SyncRequest req = new SyncRequest(obj, url);
    Assert.assertEquals(req.getRequestBody(), obj);
    Assert.assertEquals(req.getRequestUrl(), url);
    Assert.assertEquals(req.getRetryTimes().get(), 0);
    Assert.assertEquals(req.getCallBackHandler(), null);
  }
}
