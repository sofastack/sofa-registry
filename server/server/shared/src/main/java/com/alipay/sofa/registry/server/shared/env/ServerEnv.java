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
package com.alipay.sofa.registry.server.shared.env;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.alipay.sofa.registry.net.NetUtil;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:25 yuzhi.lyz Exp $
 */
public final class ServerEnv {
    private ServerEnv() {
    }

    public static final String    IP         = NetUtil.getLocalAddress().getHostAddress();
    public static final int       PID        = getPID();
    public static final ProcessId PROCESS_ID = createProcessId();

    private static ProcessId createProcessId() {
        Random random = new Random();
        return new ProcessId(IP, System.currentTimeMillis(), PID, random.nextInt(1024 * 8));
    }

    static int getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isBlank(processName)) {
            throw new RuntimeException("failed to get processName");
        }

        String[] processSplitName = processName.split("@");
        if (processSplitName.length == 0) {
            throw new RuntimeException("failed to get processName");
        }
        String pid = processSplitName[0];
        return Integer.parseInt(pid);
    }

    public static boolean isLocalServer(String ip) {
        return IP.equals(ip);
    }

    public static Collection<String> getMetaAddresses(Map<String, Collection<String>> metaMap,
                                                      String localDataCenter) {
        if (metaMap == null) {
            throw new RuntimeException("metaNodes is null");
        }
        if (localDataCenter == null) {
            throw new RuntimeException("local datacenter is null");
        }
        Collection<String> addresses = metaMap.get(localDataCenter);
        if (addresses == null || addresses.isEmpty()) {
            throw new RuntimeException(String.format("LocalDataCenter(%s) is not in metaNode",
                localDataCenter));
        }
        return addresses;
    }

    public static Set<String> transferMetaIps(Map<String, Collection<String>> metaMap, String localDataCenter) {
        Set<String> metaIps = Sets.newHashSet();
        if (metaMap != null && !metaMap.isEmpty()) {
            if (localDataCenter != null && !localDataCenter.isEmpty()) {
                Collection<String> metas = metaMap.get(localDataCenter);
                if (metas != null && !metas.isEmpty()) {
                    metas.forEach(domain -> {
                        String ip = NetUtil.getIPAddressFromDomain(domain);
                        if (ip == null) {
                            throw new RuntimeException("Meta convert domain {" + domain + "} error!");
                        }
                        metaIps.add(ip);
                    });
                }
            }
        }
        return metaIps;
    }
}
