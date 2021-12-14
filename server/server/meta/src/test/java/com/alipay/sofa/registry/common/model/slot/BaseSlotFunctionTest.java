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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.server.meta.AbstractMetaServerTestBase;
import java.util.ArrayList;

/**
 * @author chen.zhu
 *     <p>Jan 13, 2021
 */
public class BaseSlotFunctionTest extends AbstractMetaServerTestBase {

  public String[] getDataInfoIds() {
    ArrayList<String> list = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      list.add(String.format("dataInfoId-%s", i));
    }
    return list.toArray(new String[] {});
  }
}
