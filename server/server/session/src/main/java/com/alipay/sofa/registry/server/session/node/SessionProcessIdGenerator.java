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
package com.alipay.sofa.registry.server.session.node;

import com.alipay.sofa.registry.net.NetUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Session ProcessID Generator
 * @author shangyu.wh
 * @version $Id: SessionProcessId.java, v 0.1 2018-04-16 14:37 shangyu.wh Exp $
 */
public class SessionProcessIdGenerator {

    private volatile static String processId;

    private final static String    EMPTY_STRING = "";

    private final static String    PID          = getPID();

    private static AtomicInteger   count        = new AtomicInteger(1000);

    /**
     * Get session processId.
     */
    public static String getSessionProcessId() {
        if (processId == null) {
            synchronized (SessionProcessIdGenerator.class) {
                if (processId == null) {
                    processId = generate();
                }
            }
        }
        return processId;
    }

    /**
     * Generate session processId.
     */
    public static String generate() {
        String localIp = NetUtil.getLocalSocketAddress().getAddress().getHostAddress();
        if (localIp != null && !localIp.isEmpty()) {
            return getId(getIPHex(localIp), System.currentTimeMillis(), getNextId());
        }
        return EMPTY_STRING;
    }

    private static String getId(String ip, long timestamp, int nextId) {
        StringBuilder appender = new StringBuilder(30);
        appender.append(ip).append(timestamp).append(nextId).append(PID);
        return appender.toString();
    }

    private static String getIPHex(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String column : ips) {
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }

        }
        return sb.toString();
    }

    /**
     * Get process pid
     */
    public static String getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

        if (isBlank(processName)) {
            return EMPTY_STRING;
        }

        String[] processSplitName = processName.split("@");

        if (processSplitName.length == 0) {
            return EMPTY_STRING;
        }

        String pid = processSplitName[0];

        if (isBlank(pid)) {
            return EMPTY_STRING;
        }

        return pid;
    }

    private static int getNextId() {
        for (;;) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    private static boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }
}