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
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.model.SegmentData;
import com.alipay.sofa.registry.client.provider.DefaultSubscriber;
import com.alipay.sofa.registry.client.provider.RegisterCache;
import com.alipay.sofa.registry.client.task.ObserverHandler;
import com.alipay.sofa.registry.core.model.ReceivedData;
import com.alipay.sofa.registry.core.model.Result;
import java.util.List;
import org.slf4j.Logger;

/**
 * The type Received data multi processor.
 *
 * @author zhuoyu.sjw
 * @version $Id : ReceivedDataProcessor.java, v 0.1 2018-02-27 14:54 zhuoyu.sjw Exp $$
 */
public class ReceivedDataProcessor extends SyncUserProcessor<ReceivedData> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ReceivedDataProcessor.class);

  private RegisterCache registerCache;

  private ObserverHandler observerHandler;

  /**
   * Instantiates a new Received data multi processor.
   *
   * @param registerCache the register cache
   */
  public ReceivedDataProcessor(RegisterCache registerCache, ObserverHandler observerHandler) {
    this.registerCache = registerCache;
    this.observerHandler = observerHandler;
  }

  /**
   * Handle request object.
   *
   * @param bizCtx the biz ctx
   * @param request the request
   * @return the object
   */
  @Override
  public Object handleRequest(BizContext bizCtx, ReceivedData request) {
    Result result = new Result();
    result.setSuccess(true);
    if (null == request || null == request.getData()) {
      return result;
    }
    try {
      List<String> registIds = request.getSubscriberRegistIds();

      SegmentData segmentData = new SegmentData();
      segmentData.setData(request.getData());
      segmentData.setVersion(request.getVersion());
      segmentData.setSegment(request.getSegment());

      for (String registId : registIds) {
        Subscriber subscriber = registerCache.getSubscriberByRegistId(registId);
        if (null == subscriber) {
          continue;
        }

        if (subscriber instanceof DefaultSubscriber) {
          DefaultSubscriber defaultSubscriber = (DefaultSubscriber) subscriber;
          defaultSubscriber.putReceivedData(segmentData, request.getLocalZone());
          try {
            observerHandler.notify(subscriber);
          } catch (Exception e) {
            LOGGER.error(
                "[received] add notify task error, dataId: {}, registId: {}",
                subscriber.getDataId(),
                subscriber.getRegistId(),
                e);
          }
        } else {
          LOGGER.warn(
              "[received] ignore unknown subscriber type: {}", subscriber.getClass().getName());
        }
      }

      LOGGER.info(
          "[received] receive subscriber data save success, dataId: {} group: {} version: {} data:{} registIds:{}",
          request.getDataId(),
          request.getGroup(),
          request.getVersion(),
          request.getData(),
          registIds);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("");
      LOGGER.info(
          "[received] receive subscriber data save failed, dataId: {} group: {} version: {} data:{}",
          request.getDataId(),
          request.getGroup(),
          request.getVersion(),
          request.getData(),
          e);
    }
    return result;
  }

  /**
   * Interest string.
   *
   * @return the string
   */
  @Override
  public String interest() {
    return ReceivedData.class.getName();
  }
}
