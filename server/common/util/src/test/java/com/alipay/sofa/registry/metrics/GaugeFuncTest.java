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
package com.alipay.sofa.registry.metrics;

import org.junit.Assert;
import org.junit.Test;

public class GaugeFuncTest {

  @Test
  public void testGaugeFunc() {
    GaugeFunc f1 =
        GaugeFunc.build().name("gaugename").help("help").create().func(() -> 1).register();
    Assert.assertEquals(1, f1.collect().get(0).samples.size());
    GaugeFunc f2 =
        GaugeFunc.build()
            .name("gaugename1")
            .labelNames("childName")
            .help("help")
            .create()
            .register();
    f2.labels("child1").func(() -> 2);
    f2.labels("child2").func(() -> 2);
    System.out.println(f2.collect());
    Assert.assertEquals(2, f2.collect().get(0).samples.size());
  }
}
