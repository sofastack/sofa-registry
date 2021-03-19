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

import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClient;
import com.alipay.sofa.registry.client.provider.DefaultRegistryClientConfigBuilder;

/**
 * @author chen.zhu
 *     <p>Nov 18, 2020
 */
public class RegistryProvider {

  public static void main(String[] args) throws InterruptedException {

    DefaultRegistryClient registryClient =
        new DefaultRegistryClient(
            new DefaultRegistryClientConfigBuilder()
                .setRegistryEndpoint("127.0.0.1")
                .setRegistryEndpointPort(9603)
                .build());
    registryClient.init();
    // 构造发布者注册表
    String dataId = "com.alipay.test.demo.service:1.0@DEFAULT";
    PublisherRegistration registration = new PublisherRegistration(dataId);
    // 如需指定分组，可主动设置分组，未设置默认为 DEFAULT_GROUP
    registration.setGroup("TEST_GROUP");
    // 设置应用名，非必要条件
    registration.setAppName("test-app");

    // 将注册表注册进客户端获取发布者模型，并发布数据
    Publisher publisher = registryClient.register(registration, "127.0.0.1:12200?xx=zz");

    while (!Thread.currentThread().isInterrupted()) {
      Thread.sleep(10000);
    }
    // 如需覆盖上次发布的数据可以使用发布者模型重新发布数据
    // publisher.republish("10.10.1.1:12200?xx=zz");
  }
}
