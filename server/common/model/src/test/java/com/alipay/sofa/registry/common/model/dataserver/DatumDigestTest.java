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
package com.alipay.sofa.registry.common.model.dataserver;

import org.junit.Assert;
import org.junit.Test;

public class DatumDigestTest {
  @Test
  public void test() {
    DatumDigest datumDigest1 = new DatumDigest(1, 2, 3, 4, (short) 5, (short) 6);
    DatumDigest datumDigest2 = new DatumDigest(1, 2, 3, 4, (short) 5, (short) 6);
    Assert.assertEquals(datumDigest1, datumDigest2);
    Assert.assertEquals(datumDigest1.hashCode(), datumDigest2.hashCode());
    Assert.assertEquals(datumDigest1.toString(), datumDigest2.toString());

    DatumDigest datumDigest3 = new DatumDigest(2, 2, 3, 4, (short) 5, (short) 6);
    Assert.assertNotEquals(datumDigest1, datumDigest3);
    Assert.assertNotEquals(datumDigest1.toString(), datumDigest3.toString());
  }
}
