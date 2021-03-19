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
package com.alipay.sofa.registry.server.integration;

import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.SubscriberDataObserver;
import com.alipay.sofa.registry.client.api.model.UserData;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author chen.zhu
 *     <p>Nov 18, 2020
 */
public class RegistryConsumer {

  private static final Logger logger = LoggerFactory.getLogger(RegistryConsumer.class);

  public static void main(String[] args) throws InterruptedException {
    DefaultRegistryClient registryClient =
        new DefaultRegistryClient(
            new DefaultRegistryClientConfigBuilder()
                .setRegistryEndpoint("127.0.0.1")
                .setRegistryEndpointPort(9603)
                .build());
    registryClient.init();

    // 构造订阅者注册表
    String dataId = "com.alipay.test.demo.service:1.0@DEFAULT";
    SubscriberRegistration registration =
        new SubscriberRegistration(
            dataId,
            new SubscriberDataObserver() {
              @Override
              public void handleData(String dataId, UserData userData) {
                // UserData 中包含有 zoneData 和 localZone，
                // zoneData 中是服务端推送过来的多个 zone 以及 zone 内发布的数据列表
                // localZone 表示当前应用处于哪个 zone。
                logger.info("receive data success, dataId: {}, data: {}", dataId, userData);
              }
            });
    // 订阅者分组必须和发布者分组一致，未设置默认为 DEFAULT_GROUP
    registration.setGroup("TEST_GROUP");
    registration.setAppName("test-app");
    // 设置订阅维度，ScopeEnum 共有三种级别 zone, dataCenter, global。
    // zone: 逻辑机房内订阅，仅可订阅到应用所处逻辑机房内的发布者发布的数据
    // dataCenter: 机房内订阅，可订阅到应用所处机房内的发布者发布的数据
    // global: 全局订阅，可订阅到当前服务注册中心部署的所有机房内的发布者发布的数据
    registration.setScopeEnum(ScopeEnum.global);

    // 将注册表注册进客户端获取订阅者模型，订阅到的数据会以回调的方式通知 `SubscriberDataObserver`
    Subscriber subscriber = registryClient.register(registration);

    while (!Thread.currentThread().isInterrupted()) {
      Thread.sleep(10000);
    }
  }
}
