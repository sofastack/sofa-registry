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
package com.alipay.sofa.registry.common.model.slot.func;

import com.alipay.sofa.registry.common.model.slot.SlotConfig;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author chen.zhu
 *     <p>Nov 13, 2020
 */
public class Crc32cSlotFunction implements SlotFunction {
  public static final Crc32cSlotFunction INSTANCE = new Crc32cSlotFunction();
  private final int slotMask;
  private final HashFunction crc32c = Hashing.crc32c();

  public Crc32cSlotFunction() {
    this(SlotConfig.SLOT_NUM);
  }

  public Crc32cSlotFunction(int slotNums) {
    this.slotMask = slotNums - 1;
  }

  @Override
  public String name() {
    return "crc32c";
  }

  @Override
  public int maxSlots() {
    return slotMask + 1;
  }

  @Override
  public int slotOf(Object o) {
    return getCrc32Code(o) & slotMask;
  }

  private int getCrc32Code(Object o) {
    byte[] bytes = o.toString().getBytes(UTF8);
    return Math.abs(crc32c.hashBytes(bytes).asInt());
  }

  private static final Charset UTF8 = StandardCharsets.UTF_8;
}
