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
package com.alipay.sofa.registry.server.data.remoting.sessionserver.disconnect;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author qian.lqlq
 * @version $Id: DisconnectEvent.java, v 0.1 2018-04-14 18:22 qian.lqlq Exp $
 */
public class DisconnectEvent implements Delayed {

    private int                timeoutMs;

    private long               gmtOccur;

    private long               registerTimestamp;

    private DisconnectTypeEnum type;

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(getGmtOccur() + getTimeoutMs() - System.currentTimeMillis(),
            TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == this) {
            return 0;
        }
        if (o instanceof DisconnectEvent) {
            DisconnectEvent other = (DisconnectEvent) o;
            if (this.gmtOccur < other.gmtOccur) {
                return -1;
            } else if (this.gmtOccur > other.gmtOccur) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    /**
     * Getter method for property <tt>timeoutMs</tt>.
     *
     * @return property value of timeoutMs
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * Setter method for property <tt>timeoutMs</tt>.
     *
     * @param timeoutMs  value to be assigned to property timeoutMs
     */
    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Getter method for property <tt>gmtOccur</tt>.
     *
     * @return property value of gmtOccur
     */
    public long getGmtOccur() {
        return gmtOccur;
    }

    /**
     * Setter method for property <tt>gmtOccur</tt>.
     *
     * @param gmtOccur  value to be assigned to property gmtOccur
     */
    public void setGmtOccur(long gmtOccur) {
        this.gmtOccur = gmtOccur;
    }

    /**
     * Getter method for property <tt>registerTimestamp</tt>.
     *
     * @return property value of registerTimestamp
     */
    public long getRegisterTimestamp() {
        return registerTimestamp;
    }

    /**
     * Setter method for property <tt>registerTimestamp</tt>.
     *
     * @param registerTimestamp  value to be assigned to property registerTimestamp
     */
    public void setRegisterTimestamp(long registerTimestamp) {
        this.registerTimestamp = registerTimestamp;
    }

    /**
     * Getter method for property <tt>type</tt>.
     *
     * @return property value of type
     */
    public DisconnectTypeEnum getType() {
        return type;
    }

    /**
     * Setter method for property <tt>type</tt>.
     *
     * @param type  value to be assigned to property type
     */
    public void setType(DisconnectTypeEnum type) {
        this.type = type;
    }
}