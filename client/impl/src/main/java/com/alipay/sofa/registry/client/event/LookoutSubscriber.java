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
package com.alipay.sofa.registry.client.event;

import com.alipay.lookout.api.Id;
import com.alipay.lookout.api.Lookout;
import com.alipay.lookout.api.composite.MixinMetric;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.EventSubscriber;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.model.Event;
import com.alipay.sofa.registry.client.util.StringUtils;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuoyu.sjw
 * @version $Id: LookoutSubscriber.java, v 0.1 2018-07-13 20:31 zhuoyu.sjw Exp $$
 */
public class LookoutSubscriber implements EventSubscriber {

  /** */
  private static final String METRIC_DATA_ID_NAME = "data_id";

  /** */
  private static final String METRIC_INSTANCE_ID_NAME = "instance_id";

  /** */
  private static final String METRIC_COUNT_NAME = "count";

  /** */
  private static final String METRIC_TIME_NAME = "time";

  /** */
  private static final String METRIC_SUCCESS_NAME = "success";

  /** */
  private static final String REGISTRY_PROCESS_SUBSCRIBER = "registry.process.subscriber";

  /** */
  private static final String REGISTRY_PROCESS_CONFIGURATOR = "registry.process.configurator";

  /** @see EventSubscriber#isSync() */
  @Override
  public boolean isSync() {
    return false;
  }

  /** @see EventSubscriber#onEvent(Event) */
  @Override
  public void onEvent(Event event) {
    if (null == event) {
      return;
    }
    Class eventClass = event.getClass();

    if (eventClass == SubscriberProcessEvent.class) {
      SubscriberProcessEvent subscriberProcessEvent = (SubscriberProcessEvent) event;
      Subscriber subscriber = subscriberProcessEvent.getSubscriber();
      if (null == subscriber) {
        return;
      }

      RegistryClientConfig config = subscriberProcessEvent.getConfig();
      if (null == config) {
        return;
      }

      Id id =
          Lookout.registry()
              .createId(REGISTRY_PROCESS_SUBSCRIBER)
              .withTag(METRIC_DATA_ID_NAME, StringUtils.defaultString(subscriber.getDataId()))
              .withTag(METRIC_INSTANCE_ID_NAME, StringUtils.defaultString(config.getInstanceId()));
      MixinMetric mixin = Lookout.registry().mixinMetric(id);

      mixin.counter(METRIC_COUNT_NAME).inc();
      mixin
          .timer(METRIC_TIME_NAME)
          .record(
              subscriberProcessEvent.getEnd() - subscriberProcessEvent.getStart(),
              TimeUnit.MILLISECONDS);

      if (null == subscriberProcessEvent.getThrowable()) {
        mixin.counter(METRIC_SUCCESS_NAME).inc();
      }
    } else if (eventClass == ConfiguratorProcessEvent.class) {
      ConfiguratorProcessEvent configuratorProcessEvent = (ConfiguratorProcessEvent) event;
      Configurator configurator = configuratorProcessEvent.getConfigurator();
      if (null == configurator) {
        return;
      }

      RegistryClientConfig config = configuratorProcessEvent.getConfig();

      Id id =
          Lookout.registry()
              .createId(REGISTRY_PROCESS_CONFIGURATOR)
              .withTag(METRIC_DATA_ID_NAME, StringUtils.defaultString(configurator.getDataId()))
              .withTag(METRIC_INSTANCE_ID_NAME, StringUtils.defaultString(config.getInstanceId()));
      MixinMetric mixin = Lookout.registry().mixinMetric(id);

      mixin.counter(METRIC_COUNT_NAME).inc();
      mixin
          .timer(METRIC_TIME_NAME)
          .record(
              configuratorProcessEvent.getEnd() - configuratorProcessEvent.getStart(),
              TimeUnit.MILLISECONDS);

      if (null == configuratorProcessEvent.getThrowable()) {
        mixin.counter(METRIC_SUCCESS_NAME).inc();
      }
    }
  }
}
