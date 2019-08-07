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
 * @version $Id: SessionServerDisconnectEvent.java, v 0.1 2018-04-14 18:22 qian.lqlq Exp $
 */
public class SessionServerDisconnectEvent extends DisconnectEvent {

    private String processId;

    private String sessionServerHost;

    /**
     * constructor
     * @param processId
     * @param timeoutMs
     */
    public SessionServerDisconnectEvent(String processId, String sessionServerHost, int timeoutMs) {
        this.processId = processId;
        this.sessionServerHost = sessionServerHost;
        setGmtOccur(System.currentTimeMillis());
        setRegisterTimestamp(getGmtOccur());
        setTimeoutMs(timeoutMs);
        setType(DisconnectTypeEnum.SESSION_SERVER);
    }

    /**
     * Getter method for property <tt>processId</tt>.
     *
     * @return property value of processId
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Getter method for property <tt>sessionServerHost</tt>.
     *
     * @return property value of sessionServerHost
     */
    public String getSessionServerHost() {
        return sessionServerHost;
    }

}