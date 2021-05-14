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
package com.alipay.sofa.registry.server.shared.env;

import com.alipay.sofa.registry.server.shared.TestUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class ServerEnvTest {
  @Test
  public void testMetas() {
    Map<String, Collection<String>> m = Maps.newHashMap();
    m.put("localDc", Collections.EMPTY_LIST);
    TestUtils.assertRunException(
        RuntimeException.class, () -> ServerEnv.getMetaAddresses(m, "localDc"));
    Set<String> meta = Sets.newHashSet("test");
    m.put("localDc", meta);
    Collection<String> ret = ServerEnv.getMetaAddresses(m, "localDc");
    Assert.assertEquals(ret, meta);
  }

  @Test
  public void testRelease() {
    Map<String, Object> m = ServerEnv.getReleaseProps();
    System.out.println("release:" + m);
  }
}
