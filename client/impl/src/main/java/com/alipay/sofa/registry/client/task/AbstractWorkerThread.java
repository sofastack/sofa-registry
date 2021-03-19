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

import com.alipay.sofa.registry.client.remoting.Client;

/**
 * The type Abstract worker thread.
 *
 * @author zhuoyu.sjw
 * @version $Id : AbstractWorkerThread.java, v 0.1 2018-03-01 11:54 zhuoyu.sjw Exp $$
 */
public abstract class AbstractWorkerThread extends Thread implements Worker {

  /** */
  private final Object bell = new Object();

  /** The Client. */
  protected Client client;

  /**
   * Instantiates a new Abstract worker thread.
   *
   * @param client the client
   */
  public AbstractWorkerThread(Client client) {
    this.client = client;
  }

  /** Notify execute signal for task thread. */
  void signal() {
    synchronized (bell) {
      bell.notifyAll();
    }
  }

  /**
   * Await for next task.
   *
   * @param timeout wait timeout
   * @throws InterruptedException thread interrupted
   */
  void await(long timeout) throws InterruptedException {
    synchronized (bell) {
      bell.wait(timeout);
    }
  }

  /** @see Runnable#run() */
  @Override
  public void run() {
    handle();
  }
}
