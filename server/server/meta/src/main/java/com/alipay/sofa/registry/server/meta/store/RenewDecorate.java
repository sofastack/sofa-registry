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
package com.alipay.sofa.registry.server.meta.store;

import java.io.Serializable;

/**
 * heartbeat info for node
 * @author shangyu.wh
 * @version $Id: ReNewer.java, v 0.1 2018-01-16 17:10 shangyu.wh Exp $
 */
public class RenewDecorate<T> implements Serializable {

    public static final int DEFAULT_DURATION_SECS = 15;

    private T               renewal;

    private long            beginTimestamp;

    private volatile long   lastUpdateTimestamp;

    private long            duration;

    /**
     * use for task parameter
     * @param renewal
     */
    public RenewDecorate(T renewal) {
        this.renewal = renewal;
    }

    /**
     * constructor
     * @param renewal
     * @param durationSECS
     */
    public RenewDecorate(T renewal, long durationSECS) {
        this.renewal = renewal;
        this.beginTimestamp = System.currentTimeMillis();
        this.lastUpdateTimestamp = beginTimestamp;
        this.duration = durationSECS * 1000;
    }

    /**
     * verify expired or not
     * @return
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > lastUpdateTimestamp + duration;
    }

    /**
     * refresh lastUpdateTimestamp
     */
    public void reNew() {
        lastUpdateTimestamp = System.currentTimeMillis() + duration;
    }

    /**
     * refresh lastUpdateTimestamp by durationSECS
     * @param durationSECS
     */
    public void reNew(long durationSECS) {
        lastUpdateTimestamp = System.currentTimeMillis() + durationSECS * 1000;
    }

    /**
     * Getter method for property <tt>renewal</tt>.
     *
     * @return property value of renewal
     */
    public T getRenewal() {
        return renewal;
    }

    /**
     * Setter method for property <tt>renewal</tt>.
     *
     * @param renewal  value to be assigned to property renewal
     */
    public void setRenewal(T renewal) {
        this.renewal = renewal;
    }

    /**
     * Getter method for property <tt>beginTimestamp</tt>.
     *
     * @return property value of beginTimestamp
     */
    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    /**
     * Getter method for property <tt>lastUpdateTimestamp</tt>.
     *
     * @return property value of lastUpdateTimestamp
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
}