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
import com.alipay.sofa.registry.util.SystemUtils;
import java.io.Serializable;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-13 20:00 yuzhi.lyz Exp $
 */
public final class SlotConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlotConfig.class);

  public static final String KEY_DATA_SLOT_NUM = "registry.data.slot.num";
  private static final String KEY_DATA_SLOT_FUNC = "registry.data.slot.func";
  private static final String KEY_DATA_SLOT_REPLICAS = "registry.data.slot.replicas";
  public static final int SLOT_NUM;
  public static final int SLOT_REPLICAS;
  public static final String FUNC;

  private SlotConfig() {}

  static {
    SLOT_NUM = SystemUtils.getSystemInteger(KEY_DATA_SLOT_NUM, 256);
    if (SLOT_NUM <= 0) {
      throw new IllegalArgumentException("illegal " + KEY_DATA_SLOT_NUM + ":" + SLOT_NUM);
    }
    SLOT_REPLICAS = SystemUtils.getSystemInteger(KEY_DATA_SLOT_REPLICAS, 2);
    if (SLOT_REPLICAS <= 0) {
      throw new IllegalArgumentException("illegal " + KEY_DATA_SLOT_REPLICAS + ":" + SLOT_REPLICAS);
    }
    FUNC = SystemUtils.getSystem(KEY_DATA_SLOT_FUNC, "crc32c");
    LOGGER.info(
        "init slot config, {}={}, {}={}, {}={}",
        KEY_DATA_SLOT_NUM,
        SLOT_NUM,
        KEY_DATA_SLOT_REPLICAS,
        SLOT_REPLICAS,
        KEY_DATA_SLOT_FUNC,
        FUNC);
  }

  public static SlotBasicInfo slotBasicInfo() {
    return new SlotBasicInfo(SLOT_NUM, SLOT_REPLICAS, FUNC);
  }

  public static class SlotBasicInfo implements Serializable {

    private final int slotNum;

    private final int slotReplicas;

    private final String slotFunc;

    /**
     * Constructor.
     *
     * @param slotNum the slot num
     * @param slotReplicas the slot replicas
     * @param slotFunc the slot func
     */
    public SlotBasicInfo(int slotNum, int slotReplicas, String slotFunc) {
      this.slotNum = slotNum;
      this.slotReplicas = slotReplicas;
      this.slotFunc = slotFunc;
    }

    /**
     * Gets get slot num.
     *
     * @return the get slot num
     */
    public int getSlotNum() {
      return slotNum;
    }

    /**
     * Gets get slot replicas.
     *
     * @return the get slot replicas
     */
    public int getSlotReplicas() {
      return slotReplicas;
    }

    /**
     * Gets get slot func.
     *
     * @return the get slot func
     */
    public String getSlotFunc() {
      return slotFunc;
    }
  }
}
