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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.remoting.ServerManager;
import com.alipay.sofa.registry.client.remoting.ServerNode;
import com.alipay.sofa.registry.client.util.HttpClientUtils;
import com.alipay.sofa.registry.client.util.ServerNodeParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;

/**
 * The type Default server manager.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultServerManager.java, v 0.1 2017-12-25 11:48 zhuoyu.sjw Exp $$
 */
public class DefaultServerManager implements ServerManager {

  /** The constant MIN_RETRY_INTERVAL. */
  public static final int MIN_RETRY_INTERVAL = 10000;
  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerManager.class);

  private RegistryClientConfig config;
  private Set<ServerNode> serverNodes;
  private SyncServerListThread syncServerListTask;

  private AtomicBoolean inited = new AtomicBoolean(false);

  /**
   * Instantiates a new Default server manager.
   *
   * @param config the config
   */
  public DefaultServerManager(RegistryClientConfig config) {
    this.config = config;
    this.serverNodes = new HashSet<ServerNode>();
    this.syncServerListTask = new SyncServerListThread(config);
  }

  /** @see ServerManager#getServerList() */
  @Override
  public List<ServerNode> getServerList() {
    // init once
    if (inited.compareAndSet(false, true)) {
      this.syncServerListTask.start();
    }
    // sync query when server list is empty
    if (serverNodes.isEmpty()) {
      syncServerList();
    }
    return new ArrayList<ServerNode>(serverNodes);
  }

  /** @see ServerManager#random() */
  @Override
  public ServerNode random() {
    List<ServerNode> urls = getServerList();
    if (null == urls || urls.size() == 0) {
      return null;
    }
    Random random = new Random();
    return urls.get(random.nextInt(urls.size()));
  }

  private void syncServerList() {
    String url =
        String.format(
            "http://%s:%d/api/servers/query",
            config.getRegistryEndpoint(), config.getRegistryEndpointPort());
    Map<String, String> params = new HashMap<String, String>();
    params.put("env", config.getEnv());
    params.put("zone", config.getZone());
    params.put("dataCenter", config.getDataCenter());
    params.put("appName", config.getAppName());
    params.put("instanceId", config.getInstanceId());
    try {
      String result = HttpClientUtils.get(url, params, config);
      if (null != result) {
        String[] servers = result.split(";");
        Set<ServerNode> tempNodes = new HashSet<ServerNode>();
        for (String server : servers) {
          try {
            ServerNode serverNode = ServerNodeParser.parse(server);
            tempNodes.add(serverNode);
          } catch (Exception e) {
            LOGGER.error("[serverManager] parse server node error, {}", server, e);
          }
        }
        if (!tempNodes.equals(serverNodes)) {
          serverNodes = tempNodes;
          LOGGER.info("[serverManager] update nodes success, {}", tempNodes);
        }
      }
    } catch (Exception e) {
      LOGGER.error("[serverManager] get server list error", e);
    }
  }

  /** The type Sync server list task. */
  class SyncServerListThread extends Thread {

    private RegistryClientConfig config;

    /**
     * Instantiates a new Sync server list task.
     *
     * @param config the config
     */
    public SyncServerListThread(RegistryClientConfig config) {
      this.setName("SyncServerListThread");
      this.setDaemon(true);
      this.config = config;
    }

    /** @see Thread#run() */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
      int retryInterval;
      while (true) {
        try {
          retryInterval = Math.max(MIN_RETRY_INTERVAL, config.getSyncConfigRetryInterval());
          Thread.sleep(retryInterval);

          syncServerList();
        } catch (Throwable e) {
          LOGGER.error("[serverManager] sync server list task error", e);
        }
      }
    }
  }
}
