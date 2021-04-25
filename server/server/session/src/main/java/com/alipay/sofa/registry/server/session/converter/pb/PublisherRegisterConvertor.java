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
import com.alipay.sofa.registry.common.model.client.pb.PublisherRegisterPb;
import com.alipay.sofa.registry.core.model.PublisherRegister;

/**
 * @author bystander
 * @version $Id: PublisherRegisterConvertor.java, v 0.1 2018年03月21日 2:06 PM bystander Exp $
 */
public final class PublisherRegisterConvertor {
  private PublisherRegisterConvertor() {}

  public static PublisherRegisterPb convert2Pb(PublisherRegister publisherRegisterJava) {

    if (publisherRegisterJava == null) {
      return null;
    }

    PublisherRegisterPb.Builder builder = PublisherRegisterPb.newBuilder();

    BaseRegisterPb.Builder baseRegisterBuilder = BaseRegisterPb.newBuilder();

    baseRegisterBuilder
        .setAppName(publisherRegisterJava.getAppName())
        .setClientId(publisherRegisterJava.getClientId())
        .setDataId(publisherRegisterJava.getDataId())
        .setDataInfoId(publisherRegisterJava.getDataInfoId())
        .setEventType(publisherRegisterJava.getEventType())
        .setGroup(publisherRegisterJava.getGroup())
        .setInstanceId(publisherRegisterJava.getInstanceId())
        .setIp(publisherRegisterJava.getIp())
        .setPort(publisherRegisterJava.getPort())
        .setProcessId(publisherRegisterJava.getProcessId())
        .setRegistId(publisherRegisterJava.getRegistId())
        .setVersion(publisherRegisterJava.getVersion())
        .setTimestamp(publisherRegisterJava.getTimestamp())
        .putAllAttributes(publisherRegisterJava.getAttributes());

    if (publisherRegisterJava.getZone() != null) {
      baseRegisterBuilder.setZone(publisherRegisterJava.getZone());
    }

    builder.setBaseRegister(baseRegisterBuilder.build());

    builder.addAllDataList(DataBoxConvertor.convert2Pbs(publisherRegisterJava.getDataList()));
    return builder.build();
  }

  public static PublisherRegister convert2Java(PublisherRegisterPb publisherRegisterPb) {

    if (publisherRegisterPb == null) {
      return null;
    }

    PublisherRegister publisherRegister = new PublisherRegister();

    publisherRegister.setAppName(publisherRegisterPb.getBaseRegister().getAppName());
    publisherRegister.setClientId(publisherRegisterPb.getBaseRegister().getClientId());
    publisherRegister.setDataId(publisherRegisterPb.getBaseRegister().getDataId());
    publisherRegister.setDataInfoId(publisherRegisterPb.getBaseRegister().getDataInfoId());
    publisherRegister.setEventType(publisherRegisterPb.getBaseRegister().getEventType());
    publisherRegister.setGroup(publisherRegisterPb.getBaseRegister().getGroup());
    publisherRegister.setInstanceId(publisherRegisterPb.getBaseRegister().getInstanceId());
    publisherRegister.setIp(publisherRegisterPb.getBaseRegister().getIp());
    publisherRegister.setPort(publisherRegisterPb.getBaseRegister().getPort());
    publisherRegister.setProcessId(publisherRegisterPb.getBaseRegister().getProcessId());
    publisherRegister.setRegistId(publisherRegisterPb.getBaseRegister().getRegistId());
    publisherRegister.setTimestamp(publisherRegisterPb.getBaseRegister().getTimestamp());
    publisherRegister.setVersion(publisherRegisterPb.getBaseRegister().getVersion());
    publisherRegister.setDataList(
        DataBoxConvertor.convertBoxes2Javas(publisherRegisterPb.getDataListList()));
    publisherRegister.setAttributes(publisherRegisterPb.getBaseRegister().getAttributesMap());
    if (publisherRegisterPb.getBaseRegister().getZone() != null) {
      publisherRegister.setZone(publisherRegisterPb.getBaseRegister().getZone());
    }
    return publisherRegister;
  }
}
