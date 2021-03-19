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

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.model.ConfiguratorData;
import com.alipay.sofa.registry.client.provider.DefaultConfigurator;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.task.ObserverHandler;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.Result;
import java.util.List;
import org.slf4j.Logger;

/**
 * The type Received config data processor.
 *
 * @author zhuoyu.sjw
 * @version $Id : ReceivedConfigDataProcessor.java, v 0.1 2018-04-18 15:40 zhuoyu.sjw Exp $$
 */
public class ReceivedConfigDataProcessor extends SyncUserProcessor<ReceivedConfigData> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReceivedConfigDataProcessor.class);

  private RegisterCache registerCache;

  private ObserverHandler observerHandler;

  /**
   * Instantiates a new Received config data processor.
   *
   * @param registerCache the register cache
   * @param observerHandler the observer handler
   */
  public ReceivedConfigDataProcessor(RegisterCache registerCache, ObserverHandler observerHandler) {
    this.registerCache = registerCache;
    this.observerHandler = observerHandler;
  }

  /** @see com.alipay.remoting.rpc.protocol.UserProcessor#handleRequest(BizContext, Object) */
  @Override
  public Object handleRequest(BizContext bizCtx, ReceivedConfigData request) {
    Result result = new Result();
    result.setSuccess(true);
    if (null == request) {
      return result;
    }

    List<String> registIds = request.getConfiguratorRegistIds();
    try {

      ConfiguratorData configuratorData = new ConfiguratorData();
      configuratorData.setDataBox(request.getDataBox());
      configuratorData.setVersion(request.getVersion());
      for (String registId : registIds) {
        Configurator configurator = registerCache.getConfiguratorByRegistId(registId);
        if (null == configurator) {
          continue;
        }

        if (configurator instanceof DefaultConfigurator) {
          ((DefaultConfigurator) configurator).putConfiguratorData(configuratorData);
          try {
            observerHandler.notify(configurator);
          } catch (Exception e) {
            LOGGER.error(
                "[received] add configurator notify task error, dataId: {}, registId: {}",
                configurator.getDataId(),
                configurator.getRegistId(),
                e);
          }
        } else {
          LOGGER.warn(
              "[received] ignore unknown configurator type: {}", configurator.getClass().getName());
        }
      }
      LOGGER.info(
          "[received] receive configurator data save success, dataId: {} version: {} data:{} registIds:{}",
          request.getDataId(),
          request.getVersion(),
          request.getDataBox(),
          registIds);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("");
      LOGGER.info(
          "[received] receive configurator data save failed, dataId: {} version: {} data:{}",
          request.getDataId(),
          request.getVersion(),
          request.getDataBox(),
          e);
    }
    return result;
  }

  /** @see com.alipay.remoting.rpc.protocol.UserProcessor#interest() */
  @Override
  public String interest() {
    return ReceivedConfigData.class.getName();
  }
}
