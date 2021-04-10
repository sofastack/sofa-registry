/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.jdbc.cache;

import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author xiaojian.xj
 * @version $Id: HeartbeatCacheTest.java, v 0.1 2021年04月10日 16:37 xiaojian.xj Exp $
 */
public class HeartbeatCacheTest {

  @SuppressWarnings({"unchecked"})
  private RemovalListener<String, String> removalListener = Mockito.mock(RemovalListener.class);

  Ticker ticker = Mockito.mock(Ticker.class);

  private LoadingCache<String, String> cache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(3, TimeUnit.SECONDS)
          // .removalListener(removalListener)
          // .ticker(ticker)
          .build(
              new CacheLoader<String, String>() {
                @Override
                public String load(String revision) {
                  return "";
                }
              });

  @Test
  public void heartbeatCacheClean() throws InterruptedException {

    cache.put("foo", "bar");
    Thread.sleep(1000);
    Map<String, String> map = new HashMap<>(cache.asMap());
    map.forEach(
        (key, value) -> {
          Assert.assertEquals(key, "foo");
        });
    map.get("foo");

    Thread.sleep(2500);
    String val = cache.getIfPresent("foo");
    Assert.assertTrue(val == null);
    Assert.assertEquals(cache.asMap().size(), 0);
  }
}
