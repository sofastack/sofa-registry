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
package com.alipay.sofa.registry.client.remoting;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.task.TaskEvent;
import com.alipay.sofa.registry.client.task.Worker;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.slf4j.Logger;

/**
 * The type Client connection.
 *
 * @author zhuoyu.sjw
 * @version $Id : ClientConnection.java, v 0.1 2018-03-01 16:44 zhuoyu.sjw Exp $$
 */
public class ClientConnection implements Client {
  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class);
  /** The Reconnecting delay. */
  private static final int RECONNECTING_DELAY = 5000;

  private RpcClient client;
  private ServerManager serverManager;
  private List<UserProcessor> userProcessorList;
  private Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap;
  private RegistryClientConfig config;
  private Connection clientConnection;
  private RegisterCache registerCache;
  private Worker worker;

  /**
   * Instantiates a new Client connection.
   *
   * @param serverManager the server manager
   * @param userProcessorList the user processor list
   * @param connectionEventProcessorMap the connection event processor map
   * @param config the config
   */
  public ClientConnection(
      ServerManager serverManager,
      List<UserProcessor> userProcessorList,
      Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap,
      RegisterCache registerCache,
      RegistryClientConfig config) {
    this.client = new RpcClient();
    this.serverManager = serverManager;
    this.userProcessorList = userProcessorList;
    this.connectionEventProcessorMap = connectionEventProcessorMap;
    this.registerCache = registerCache;
    this.config = config;
  }

  /** Init. */
  @Override
  public void init() {
    for (UserProcessor userProcessor : userProcessorList) {
      client.registerUserProcessor(userProcessor);
    }

    if (null != connectionEventProcessorMap) {
      for (Entry<ConnectionEventType, ConnectionEventProcessor> entry :
          connectionEventProcessorMap.entrySet()) {
        client.addConnectionEventProcessor(entry.getKey(), entry.getValue());
      }
    }

    client.init();
  }

  /**
   * Ensure connected.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Override
  public void ensureConnected() throws InterruptedException {
    if (isConnected()) {
      return;
    }
    while (!connect()) {
      Thread.sleep(ClientConnection.RECONNECTING_DELAY);
    }
  }

  /**
   * Connect boolean.
   *
   * @return the boolean
   */
  private boolean connect() {
    Random random = new Random();
    Connection connection = null;
    List<ServerNode> serverNodes = new ArrayList<ServerNode>(serverManager.getServerList());
    // shuffle server list to make server connections as discrete as possible
    Collections.shuffle(serverNodes);
    for (ServerNode serverNode : serverNodes) {
      try {
        connection = connect(serverNode);
        if (null != connection && connection.isFine()) {
          resetRegister();
          LOGGER.info("[Connect] Successfully connected to server: {}", serverNode);
          break;
        } else {
          recycle(connection);
        }

        Thread.sleep(random.nextInt(RECONNECTING_DELAY));
      } catch (Exception e) {
        LOGGER.error("[Connect] Failed trying connect to {}", serverNode, e);
      }
    }

    if (null != connection && connection.isFine()) {
      clientConnection = connection;
      return true;
    }
    return false;
  }

  /**
   * Invoke sync object.
   *
   * @param request the request
   * @return the object
   * @throws RemotingException the remoting exception
   * @throws InterruptedException the interrupted exception
   */
  @Override
  public Object invokeSync(Object request) throws RemotingException, InterruptedException {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected");
    }

    return client.invokeSync(clientConnection, request, config.getInvokeTimeout());
  }

  private void recycle(Connection connection) {
    if (null == connection) {
      return;
    }

    client.closeConnection(connection.getUrl());
  }

  private Connection connect(ServerNode serverNode) {
    Connection connection = null;
    try {
      connection = client.getConnection(serverNode.getUrl(), config.getConnectTimeout());
    } catch (Exception e) {
      LOGGER.error("[connection] Create connection error, {}", serverNode, e);
    }
    return connection;
  }

  private void resetRegister() {
    try {
      List<TaskEvent> eventList = new ArrayList<TaskEvent>();

      Collection<Publisher> publishers = registerCache.getAllPublishers();
      for (Publisher publisher : publishers) {
        try {
          publisher.reset();
          eventList.add(new TaskEvent(publisher));
        } catch (Exception e) {
          LOGGER.error("[connection] Publisher reset error, {}", publisher, e);
        }
      }

      Collection<Subscriber> subscribers = registerCache.getAllSubscribers();
      for (Subscriber subscriber : subscribers) {
        try {
          subscriber.reset();
          eventList.add(new TaskEvent(subscriber));
        } catch (Exception e) {
          LOGGER.error("[connection] Subscriber reset error, {}", subscriber, e);
        }
      }

      Collection<Configurator> configurators = registerCache.getAllConfigurator();
      for (Configurator configurator : configurators) {
        try {
          configurator.reset();
          eventList.add(new TaskEvent(configurator));
        } catch (Exception e) {
          LOGGER.error("[connection] Configurator reset error, {}", configurator, e);
        }
      }

      worker.schedule(eventList);
      LOGGER.info(
          "[reset] {} publishers and {} subscribers has been reset",
          publishers.size(),
          subscribers.size());
    } catch (Exception e) {
      LOGGER.error("[reset] Reset register after reconnect error", e);
    }
  }

  /**
   * Gets remote address.
   *
   * @return the remote address
   */
  public String getRemoteAddress() {
    if (null != clientConnection) {
      return clientConnection.getRemoteIP();
    }
    return null;
  }

  /**
   * Is connected boolean.
   *
   * @return boolean boolean
   */
  @Override
  public boolean isConnected() {
    return clientConnection != null && clientConnection.isFine();
  }

  /** Destroy. */
  public void destroy() {
    if (null != clientConnection) {
      clientConnection.close();
    }
    if (null != client) {
      client.shutdown();
    }
  }

  /**
   * Setter method for property <tt>serverManager</tt>.
   *
   * @param serverManager value to be assigned to property serverManager
   */
  public void setServerManager(ServerManager serverManager) {
    this.serverManager = serverManager;
  }

  /**
   * Setter method for property <tt>userProcessorList</tt>.
   *
   * @param userProcessorList value to be assigned to property userProcessorList
   */
  public void setUserProcessorList(List<UserProcessor> userProcessorList) {
    this.userProcessorList = userProcessorList;
  }

  /**
   * Setter method for property <tt>connectionEventProcessorMap</tt>.
   *
   * @param connectionEventProcessorMap value to be assigned to property connectionEventProcessorMap
   */
  public void setConnectionEventProcessorMap(
      Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap) {
    this.connectionEventProcessorMap = connectionEventProcessorMap;
  }

  /**
   * Setter method for property <tt>worker</tt>.
   *
   * @param worker value to be assigned to property worker
   */
  public void setWorker(Worker worker) {
    this.worker = worker;
  }
}
