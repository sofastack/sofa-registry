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

/**
 *
 * @author qian.lqlq
 * @version $Id: ClientDisconnectEvent.java, v 0.1 2018-04-14 18:22 qian.lqlq Exp $
 */
public class ClientDisconnectEvent extends DisconnectEvent {

    private String host;

    /**
     * constructor
     * @param host
     * @param registerTimestamp
     * @param timeoutMs
     */
    public ClientDisconnectEvent(String host, long registerTimestamp, int timeoutMs) {
        this.host = host;
        setRegisterTimestamp(registerTimestamp);
        setGmtOccur(System.currentTimeMillis());
        setTimeoutMs(timeoutMs);
        setType(DisconnectTypeEnum.CLIENT);
    }

    /**
     * Getter method for property <tt>host</tt>.
     *
     * @return property value of host
     */
    public String getHost() {
        return host;
    }

}