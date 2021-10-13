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
package com.alipay.sofa.registry.common.model.metaserver;

import com.google.common.collect.Sets;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;

public class CompressPushSwitchTest {
  @Test
  public void test() {
    CompressPushSwitch compressPushSwitch = CompressPushSwitch.defaultSwitch();
    Assert.assertFalse(compressPushSwitch.isEnabled());
    compressPushSwitch.setEnabled(true);
    Assert.assertTrue(compressPushSwitch.isEnabled());
    compressPushSwitch.setEnabledClients(new HashSet<>());
    Assert.assertEquals(compressPushSwitch.getEnabledClients().size(), 0);
    compressPushSwitch.setEnabledSessions(new HashSet<>());
    Assert.assertEquals(compressPushSwitch.getEnabledSessions().size(), 0);
    compressPushSwitch.setForbidEncodes(new HashSet<>());
    Assert.assertEquals(compressPushSwitch.getForbidEncodes().size(), 0);

    compressPushSwitch.setCompressMinSize(1000);
    Assert.assertEquals(compressPushSwitch.getCompressMinSize(), 1000);
    compressPushSwitch.setEnabledClients(Sets.newHashSet("123"));
    compressPushSwitch.setEnabledSessions(Sets.newHashSet("123"));
    compressPushSwitch.setForbidEncodes(Sets.newHashSet("123"));
  }
}
