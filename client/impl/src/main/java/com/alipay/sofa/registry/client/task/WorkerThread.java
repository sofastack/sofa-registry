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
package com.alipay.sofa.registry.client.task;

import com.alipay.sofa.registry.client.api.Register;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.provider.AbstractInternalRegister;
import com.alipay.sofa.registry.client.provider.AbstractInternalRegister.SyncTask;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.remoting.Client;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;

/**
 * The type Worker thread.
 *
 * @author zhuoyu.sjw
 * @version $Id : WorkerThread.java, v 0.1 2018-03-01 11:51 zhuoyu.sjw Exp $$
 */
public class WorkerThread extends AbstractWorkerThread {
  private static final Logger LOGGER = LoggerFactory.getLogger(WorkerThread.class);

  /** Task queue */
  protected final TaskQueue requestQueue = new TaskQueue();

  private RegistryClientConfig config;

  private RegisterCache registerCache;

  private AtomicBoolean inited = new AtomicBoolean(false);

  /**
   * Instantiates a new Worker thread.
   *
   * @param client the client connection
   * @param config the config
   */
  public WorkerThread(Client client, RegistryClientConfig config, RegisterCache registerCache) {
    super(client);
    this.config = config;
    this.registerCache = registerCache;
    this.setName("RegistryWorkerThread");
    this.setDaemon(true);
  }

  /**
   * Schedule.
   *
   * @param event the event
   */
  @Override
  public void schedule(TaskEvent event) {
    if (inited.compareAndSet(false, true)) {
      this.start();
    }
    requestQueue.put(event);
    signal();
  }

  /**
   * Schedule.
   *
   * @param events the events
   */
  @Override
  public void schedule(List<TaskEvent> events) {
    if (inited.compareAndSet(false, true)) {
      this.start();
    }
    requestQueue.putAll(events);
  }

  /** Handle. */
  @Override
  public void handle() {
    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        // check connection status, try to reconnect to the server when connection lose
        client.ensureConnected();

        if (requestQueue.isEmpty()) {
          await(config.getRecheckInterval());
          continue;
        }

        Iterator<TaskEvent> lt = requestQueue.iterator();

        while (lt.hasNext()) {
          client.ensureConnected();
          TaskEvent ev = lt.next();
          lt.remove();
          int sendCount = ev.incSendCount();

          // Resent needs delay when task event is not the first time to send.
          if (sendCount != 0 && ev.delayTime() > 0) {
            continue;
          }

          handleTask(ev);
        }

        // Cleaning completed task, it will take more time when the registration number is large.
        requestQueue.cleanCompletedTasks();
      } catch (Throwable e) {
        LOGGER.error("[send] handle data error!", e);
      }
    }
  }

  private void handleTask(TaskEvent event) {
    if (null == event) {
      return;
    }

    try {
      event.setTriggerTime(System.currentTimeMillis());
      Register register = event.getSource();

      if (!(register instanceof AbstractInternalRegister)) {
        LOGGER.warn("[register] register type unknown, {}", register);
        return;
      }

      AbstractInternalRegister abstractInternalRegister = (AbstractInternalRegister) register;

      SyncTask syncTask = abstractInternalRegister.assemblySyncTask();
      String requestId = syncTask.getRequestId();

      if (syncTask.isDone()) {
        LOGGER.info("[register] register already sync succeeded, {}", register);
        return;
      }

      Object request = syncTask.getRequest();

      Object result = client.invokeSync(request);

      if (!(result instanceof RegisterResponse)) {
        LOGGER.warn("[register] result type is wrong, {}", result);
        return;
      }

      RegisterResponse response = (RegisterResponse) result;
      if (!response.isSuccess()) {
        LOGGER.info("[register] register to server failed, {}, {}", request, response);
        return;
      }

      boolean syncOK =
          abstractInternalRegister.syncOK(requestId, response.getVersion(), response.isRefused());
      if (!syncOK) {
        LOGGER.info(
            "[register] requestId has expired, ignore this response, {}, {}, {}",
            requestId,
            request,
            response);
        return;
      }

      if (!register.isEnabled()) {
        registerCache.remove(register.getRegistId());
      }

      if (response.isRefused()) {
        LOGGER.info(
            "[register] register refused by server, {}, {}, {}", requestId, request, response);
      } else {
        LOGGER.info(
            "[register] register to server success, {}, {}, {}", requestId, request, response);
      }
    } catch (Exception e) {
      LOGGER.error("[send] handle request failed, {}", event, e);
    }
  }
}
