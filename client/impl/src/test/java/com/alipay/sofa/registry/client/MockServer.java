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
package com.alipay.sofa.registry.client;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.sofa.registry.core.constants.EventTypeConstants;
import com.alipay.sofa.registry.core.model.BaseRegister;
import com.alipay.sofa.registry.core.model.ConfiguratorRegister;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.core.model.RegisterResponse;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.core.model.SubscriberRegister;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Mock server.
 *
 * @author zhuoyu.sjw
 * @version $Id : MockServer.java, v 0.1 2017-12-25 22:39 zhuoyu.sjw Exp $$
 */
public class MockServer {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(MockServer.class);

  private RpcServer rpcServer;

  private String ip = "127.0.0.1";

  private int port = 9600;

  private Map<String, PublisherRegister> publisherMap = new HashMap<String, PublisherRegister>();
  private Map<String, SubscriberRegister> subscriberMap = new HashMap<String, SubscriberRegister>();
  private Map<String, ConfiguratorRegister> configuratorMap =
      new HashMap<String, ConfiguratorRegister>();

  /** Start. */
  public void start() {
    rpcServer = new RpcServer(port);
    rpcServer.registerUserProcessor(new MockSubscriberRegisterProcessor());
    rpcServer.registerUserProcessor(new MockPublisherRegisterProcessor());
    rpcServer.registerUserProcessor(new MockConfiguratorRegisterProcesor());
    rpcServer.start();
  }

  /** Stop. */
  public void stop() {
    rpcServer.stop();
  }

  /**
   * Getter method for property <tt>ip</tt>.
   *
   * @return property value of ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Setter method for property <tt>ip</tt>.
   *
   * @param ip value to be assigned to property ip
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Getter method for property <tt>port</tt>.
   *
   * @return property value of port
   */
  public int getPort() {
    return port;
  }

  /**
   * Setter method for property <tt>port</tt>.
   *
   * @param port value to be assigned to property port
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Query publisher by registId
   *
   * @param registId
   * @return
   */
  public PublisherRegister queryPubliser(String registId) {
    return publisherMap.get(registId);
  }

  /**
   * Query subscriber by registId
   *
   * @param registId
   * @return
   */
  public SubscriberRegister querySubscriber(String registId) {
    return subscriberMap.get(registId);
  }

  /** The type Mock subscriber register processor. */
  class MockSubscriberRegisterProcessor extends SyncUserProcessor<SubscriberRegister> {

    @Override
    public Object handleRequest(BizContext bizCtx, SubscriberRegister request) throws Exception {
      if ("subscribeAndRefused".equals(request.getDataId())) {
        return response(request, true);
      }

      Result result = new Result();
      result.setSuccess(true);
      String registId = request.getRegistId();
      if (EventTypeConstants.REGISTER.equals(request.getEventType())) {
        subscriberMap.put(registId, request);
      } else if (EventTypeConstants.UNREGISTER.equals(request.getEventType())) {
        subscriberMap.remove(registId);
      }
      return response(request);
    }

    @Override
    public String interest() {
      return SubscriberRegister.class.getName();
    }
  }

  /** The type Mock publisher register processor. */
  class MockPublisherRegisterProcessor extends SyncUserProcessor<PublisherRegister> {

    @Override
    public Object handleRequest(BizContext bizCtx, PublisherRegister request) throws Exception {
      if ("publishAndRefused".equals(request.getDataId())) {
        return response(request, true);
      }
      String registId = request.getRegistId();
      List<DataBox> dataList = request.getDataList();
      LOGGER.info(registId + " " + request.getEventType() + " " + dataList);
      if (EventTypeConstants.REGISTER.equals(request.getEventType())) {
        publisherMap.put(registId, request);
      } else if (EventTypeConstants.UNREGISTER.equals(request.getEventType())) {
        publisherMap.remove(registId);
      }
      return response(request);
    }

    @Override
    public String interest() {
      return PublisherRegister.class.getName();
    }
  }

  private RegisterResponse response(BaseRegister register) {
    return response(register, false);
  }

  private RegisterResponse response(BaseRegister register, boolean refused) {
    RegisterResponse response = new RegisterResponse();
    response.setSuccess(true);
    response.setVersion(register.getVersion());
    response.setRegistId(register.getRegistId());
    response.setRefused(refused);
    return response;
  }

  class MockConfiguratorRegisterProcesor extends SyncUserProcessor<ConfiguratorRegister> {

    @Override
    public Object handleRequest(BizContext bizCtx, ConfiguratorRegister request) throws Exception {
      if ("subscribeAndRefused".equals(request.getDataId())) {
        return response(request, true);
      }

      String registId = request.getRegistId();
      LOGGER.info("dataId: {} registId: {}", request.getDataId(), registId);
      if (EventTypeConstants.REGISTER.equals(request.getEventType())) {
        configuratorMap.put(registId, request);
      } else if (EventTypeConstants.UNREGISTER.equals(request.getEventType())) {
        configuratorMap.remove(registId);
      }
      return response(request);
    }

    @Override
    public String interest() {
      return ConfiguratorRegister.class.getName();
    }
  }
}
