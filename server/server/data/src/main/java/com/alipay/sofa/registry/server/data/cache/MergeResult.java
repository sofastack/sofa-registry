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
package com.alipay.sofa.registry.server.data.cache;

/**
 *
 * @author shangyu.wh
 * @version $Id: MergeResult.java, v 0.1 2019-02-20 17:24 shangyu.wh Exp $
 */
public class MergeResult {

    private Long    lastVersion;

    private boolean changeFlag;

    public MergeResult(Long lastVersion, boolean changeFlag) {
        this.lastVersion = lastVersion;
        this.changeFlag = changeFlag;
    }

    /**
     * Getter method for property <tt>lastVersion</tt>.
     *
     * @return property value of lastVersion
     */
    public Long getLastVersion() {
        return lastVersion;
    }

    /**
     * Setter method for property <tt>lastVersion</tt>.
     *
     * @param lastVersion  value to be assigned to property lastVersion
     */
    public void setLastVersion(Long lastVersion) {
        this.lastVersion = lastVersion;
    }

    /**
     * Getter method for property <tt>changeFlag</tt>.
     *
     * @return property value of changeFlag
     */
    public boolean isChangeFlag() {
        return changeFlag;
    }

    /**
     * Setter method for property <tt>changeFlag</tt>.
     *
     * @param changeFlag  value to be assigned to property changeFlag
     */
    public void setChangeFlag(boolean changeFlag) {
        this.changeFlag = changeFlag;
    }
}