package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.metaserver.nodes.SessionNode;
import com.alipay.sofa.registry.common.model.store.URL;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class NodeUtilsTest {
  @Test
  public void test() {
    Assert.assertTrue(NodeUtils.transferNodeToIpList(Collections.EMPTY_LIST).isEmpty());
    SessionNode node1 = new SessionNode(new URL("xx", 12), "test");
    SessionNode node2 = new SessionNode(new URL("yy", 34), "test");
    List<String> list = NodeUtils.transferNodeToIpList(Lists.newArrayList(node1, node2));
    Assert.assertEquals(list.get(0), "xx");
    Assert.assertEquals(list.get(1), "yy");
  }
}
