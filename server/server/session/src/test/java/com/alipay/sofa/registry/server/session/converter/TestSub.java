/** Alipay.com Inc. Copyright (c) 2004-2022 All Rights Reserved. */
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.util.JsonUtils;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author xiaojian.xj
 * @version : TestSub.java, v 0.1 2022年10月19日 10:44 xiaojian.xj Exp $
 */
public class TestSub {

  public static void main(String[] args) {
    Subscriber subscriber = TestUtils.newZoneSubscriber("id1", "aaa");
    subscriber.setAcceptMulti(true);
    Map<String, Long> versions = Maps.newHashMap();
    versions.put("dc1", System.nanoTime());
    versions.put("dc2", System.nanoTime());

    Map<String, Integer> nums = Maps.newHashMap();
    nums.put("dc1", 1);
    nums.put("dc2", 1);
    subscriber.checkAndUpdateCtx(versions, nums);
    System.out.println(JsonUtils.writeValueAsString(subscriber));
  }
}
