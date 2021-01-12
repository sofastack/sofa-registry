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

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 *
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-13 20:00 yuzhi.lyz Exp $
 */
public final class SlotConfig {

    private static final Logger LOGGER                 = LoggerFactory.getLogger(SlotConfig.class);

    private static final String KEY_DATA_SLOT_NUM      = "data.slot.num";
    private static final String KEY_DATA_SLOT_FUNC     = "data.slot.func";
    private static final String KEY_DATA_SLOT_REPLICAS = "data.slot.replicas";
    public static final int     SLOT_NUM;
    public static final int     SLOT_REPLICAS;
    public static final String  FUNC;

    private SlotConfig() {
    }

    static {
        String max = System.getProperty(KEY_DATA_SLOT_NUM, "16");
        SLOT_NUM = Integer.parseInt(max);
        if (SLOT_NUM <= 0) {
            throw new IllegalArgumentException("illegal " + KEY_DATA_SLOT_NUM + ":" + SLOT_NUM);
        }
        String replicas = System.getProperty(KEY_DATA_SLOT_REPLICAS, "2");
        SLOT_REPLICAS = Integer.parseInt(replicas);
        if (SLOT_REPLICAS <= 0) {
            throw new IllegalArgumentException("illegal " + KEY_DATA_SLOT_REPLICAS + ":"
                                               + SLOT_REPLICAS);
        }
        FUNC = System.getProperty(KEY_DATA_SLOT_FUNC, "crc16");
        LOGGER.info("{}={}, {}={}, {}={}", KEY_DATA_SLOT_NUM, SLOT_NUM, KEY_DATA_SLOT_REPLICAS,
            SLOT_REPLICAS, KEY_DATA_SLOT_FUNC, FUNC);
    }
}
