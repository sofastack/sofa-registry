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

import java.io.Serializable;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 10:34 yuzhi.lyz Exp $
 */
public final class SlotAccess implements Serializable {
    public enum Status {
        Accept,
        Migrating,
        Moved,
    }

    private final Status status;
    private final short  slotId;
    private final long   slotEpoch;

    public SlotAccess(short slotId, long slotEpoch, Status status) {
        this.slotEpoch = slotEpoch;
        this.slotId = slotId;
        this.status = status;
    }

    public boolean isMoved() {
        return status == Status.Moved;
    }

    public boolean isMigrating() {
        return status == Status.Migrating;
    }

    public boolean isAccept() {
        return status == Status.Accept;
    }

    public Status getStatus() {
        return status;
    }

    public short getSlotId() {
        return slotId;
    }

    public long getSlotEpoch() {
        return slotEpoch;
    }

    @Override public String toString() {
        return "SlotAccess{" +
                "status=" + status +
                ", slotId=" + slotId +
                ", slotEpoch=" + slotEpoch +
                '}';
    }
}
