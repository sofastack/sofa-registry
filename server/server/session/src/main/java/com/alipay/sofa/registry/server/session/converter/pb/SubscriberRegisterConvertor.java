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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.BaseRegisterPb;
import com.alipay.sofa.registry.common.model.client.pb.SubscriberRegisterPb;
import com.alipay.sofa.registry.core.model.SubscriberRegister;

/**
 * @author bystander
 * @version $Id: SubscriberRegisterConvertor.java, v 0.1 2018年03月21日 2:07 PM bystander Exp $
 */
public final class SubscriberRegisterConvertor {
  private SubscriberRegisterConvertor() {}

  public static SubscriberRegister convert2Java(SubscriberRegisterPb subscriberRegisterPb) {

    if (subscriberRegisterPb == null) {
      return null;
    }

    SubscriberRegister subscriberRegister = new SubscriberRegister();

    subscriberRegister.setAppName(subscriberRegisterPb.getBaseRegister().getAppName());
    subscriberRegister.setClientId(subscriberRegisterPb.getBaseRegister().getClientId());
    subscriberRegister.setDataId(subscriberRegisterPb.getBaseRegister().getDataId());
    subscriberRegister.setDataInfoId(subscriberRegisterPb.getBaseRegister().getDataInfoId());
    subscriberRegister.setEventType(subscriberRegisterPb.getBaseRegister().getEventType());
    subscriberRegister.setGroup(subscriberRegisterPb.getBaseRegister().getGroup());
    subscriberRegister.setZone(subscriberRegisterPb.getBaseRegister().getZone());
    subscriberRegister.setInstanceId(subscriberRegisterPb.getBaseRegister().getInstanceId());
    subscriberRegister.setIp(subscriberRegisterPb.getBaseRegister().getIp());
    subscriberRegister.setPort(subscriberRegisterPb.getBaseRegister().getPort());
    subscriberRegister.setProcessId(subscriberRegisterPb.getBaseRegister().getProcessId());
    subscriberRegister.setRegistId(subscriberRegisterPb.getBaseRegister().getRegistId());
    subscriberRegister.setTimestamp(subscriberRegisterPb.getBaseRegister().getTimestamp());
    subscriberRegister.setVersion(subscriberRegisterPb.getBaseRegister().getVersion());

    subscriberRegister.setAttributes(subscriberRegisterPb.getBaseRegister().getAttributesMap());
    subscriberRegister.setAcceptEncoding(subscriberRegisterPb.getAcceptEncoding());
    subscriberRegister.setScope(subscriberRegisterPb.getScope());
    subscriberRegister.setAcceptMulti(subscriberRegisterPb.getAcceptMulti());
    return subscriberRegister;
  }

  public static SubscriberRegisterPb convert2Pb(SubscriberRegister subscriberRegisterJava) {

    if (subscriberRegisterJava == null) {
      return null;
    }

    SubscriberRegisterPb.Builder builder = SubscriberRegisterPb.newBuilder();

    BaseRegisterPb.Builder baseRegisterBuilder = BaseRegisterPb.newBuilder();

    baseRegisterBuilder
        .setAppName(subscriberRegisterJava.getAppName())
        .setClientId(subscriberRegisterJava.getClientId())
        .setDataId(subscriberRegisterJava.getDataId())
        .setDataInfoId(subscriberRegisterJava.getDataInfoId())
        .setEventType(subscriberRegisterJava.getEventType())
        .setGroup(subscriberRegisterJava.getGroup())
        .setZone(subscriberRegisterJava.getZone())
        .setInstanceId(subscriberRegisterJava.getInstanceId())
        .setIp(subscriberRegisterJava.getIp())
        .setPort(subscriberRegisterJava.getPort())
        .setProcessId(subscriberRegisterJava.getProcessId())
        .setRegistId(subscriberRegisterJava.getRegistId())
        .setVersion(subscriberRegisterJava.getVersion())
        .setTimestamp(subscriberRegisterJava.getTimestamp())
        .putAllAttributes(subscriberRegisterJava.getAttributes());

    builder
        .setScope(subscriberRegisterJava.getScope())
        .setBaseRegister(baseRegisterBuilder.build())
        .setAcceptEncoding(
            subscriberRegisterJava.getAcceptEncoding() == null
                ? ""
                : subscriberRegisterJava.getAcceptEncoding());
    return builder.build();
  }
}
