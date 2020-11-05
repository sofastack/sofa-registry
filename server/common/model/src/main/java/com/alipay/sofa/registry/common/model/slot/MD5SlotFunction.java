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

import com.alipay.sofa.registry.consistency.hash.MD5HashFunction;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-02 15:43 yuzhi.lyz Exp $
 */
public final class MD5SlotFunction implements SlotFunction {
    public final  int             MAX_SLOTS       = 16384;
    private final int             maxSlots;
    private final MD5HashFunction md5HashFunction = new MD5HashFunction();

    public MD5SlotFunction() {
        this.maxSlots = MAX_SLOTS;
    }

    @Override public int maxSlots() {
        return maxSlots;
    }

    @Override
    public int slotOf(Object o) {
        // make sure >=0
        final int hash = Math.abs(md5HashFunction.hash(o));
        return hash % maxSlots;
    }
}
