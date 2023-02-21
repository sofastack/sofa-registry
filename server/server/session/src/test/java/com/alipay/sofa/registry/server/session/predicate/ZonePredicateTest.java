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
package com.alipay.sofa.registry.server.session.predicate;

import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfigBean;
import java.util.function.Predicate;
import org.junit.Assert;
import org.junit.Test;

public class ZonePredicateTest {
  @Test
  public void test() {
    SessionServerConfigBean configBean = TestUtils.newSessionConfig("testDc");
    Predicate<String> predicate =
        ZonePredicate.pushDataPredicate("testDataId", "zoneA", ScopeEnum.zone, configBean);
    Assert.assertFalse(predicate.test("zoneA"));
    Assert.assertTrue(predicate.test("zoneB"));

    predicate =
        ZonePredicate.pushDataPredicate("testDataId", "zoneA", ScopeEnum.dataCenter, configBean);
    Assert.assertFalse(predicate.test("zoneA"));
    Assert.assertFalse(predicate.test("zoneB"));

    predicate =
        ZonePredicate.pushDataPredicate("testDataId", "zoneA", ScopeEnum.global, configBean);
    Assert.assertFalse(predicate.test("zoneA"));
    Assert.assertFalse(predicate.test("zoneB"));
  }
}
