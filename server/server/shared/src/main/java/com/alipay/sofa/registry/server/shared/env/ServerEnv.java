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
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.io.InputStream;
import java.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-11-28 15:25 yuzhi.lyz Exp $
 */
public final class ServerEnv {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerEnv.class);

  private ServerEnv() {}

  public static final String GIT_PROPS_FILE = "sofaregistry.git.properties";
  public static final String IP = NetUtil.getLocalAddress().getHostAddress();
  public static final int PID = getPID();
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

  public static Collection<String> getMetaAddresses(
      Map<String, Collection<String>> metaMap, String localDataCenter) {
    ParaCheckUtil.checkNotNull(metaMap, "metaNodes");
    ParaCheckUtil.checkNotNull(localDataCenter, "localDataCenter");

    Collection<String> addresses = metaMap.get(localDataCenter);
    if (addresses == null || addresses.isEmpty()) {
      throw new RuntimeException(
          String.format("LocalDataCenter(%s) is not in metaNode", localDataCenter));
    }
    return addresses;
  }

  public static Map<String, Object> getReleaseProps() {
    return getReleaseProps(GIT_PROPS_FILE);
  }

  public static Map<String, Object> getReleaseProps(String resource) {
    InputStream inputStream = ServerEnv.class.getClassLoader().getResourceAsStream(resource);
    Properties properties = new Properties();
    if (inputStream != null) {
      try {
        properties.load(inputStream);
      } catch (Throwable e) {
        LOGGER.warn("failed to start release props file {}", resource);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
    } else {
      LOGGER.warn(
          "release props file not found:{}", ServerEnv.class.getClassLoader().getResource("/"));
    }
    return new TreeMap<String, Object>((Map) properties);
  }
}
