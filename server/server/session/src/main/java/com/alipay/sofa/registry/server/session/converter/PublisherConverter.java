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
package com.alipay.sofa.registry.server.session.converter;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shangyu.wh
 * @version $Id: PublisherConvert.java, v 0.1 2017-11-30 17:54 shangyu.wh Exp $
 */
public final class PublisherConverter {
  private PublisherConverter() {}

  private static final Converter<Tuple<PublisherRegister, Channel>, Publisher> PUBLISHER_CONVERTER =
      source -> {
        Publisher publisher = new Publisher();
        PublisherRegister register = source.getFirst();
        Channel channel = source.getSecond();
        ParaCheckUtil.checkNotNull(channel, "channel");
        fillCommonRegion(publisher, register, channel);
        publisher.setDataList(convert(register.getDataList()));

        return publisher;
      };

  private static void fillCommonRegion(
      Publisher publisher, PublisherRegister register, Channel channel) {
    publisher.setAppName(register.getAppName());
    // ZONE MUST BE CURRENT SESSION ZONE
    publisher.setCell(register.getZone());
    publisher.setClientId(register.getClientId());
    publisher.setDataId(register.getDataId());
    publisher.setGroup(register.getGroup());
    publisher.setInstanceId(register.getInstanceId());
    publisher.setRegisterId(register.getRegistId());
    publisher.setProcessId(register.getProcessId());
    if (register.getVersion() != null) {
      publisher.setVersion(register.getVersion());
    }

    // registerTimestamp must happen from server,client time maybe different cause pub and
    // unPublisher fail
    publisher.setRegisterTimestamp(System.currentTimeMillis());

    publisher.setClientRegisterTimestamp(register.getTimestamp());
    publisher.setSourceAddress(
        new URL(channel.getRemoteAddress().getHostString(), channel.getRemoteAddress().getPort()));
    publisher.setTargetAddress(
        new URL(channel.getLocalAddress().getHostString(), channel.getLocalAddress().getPort()));
    publisher.setIp(register.getIp());
    publisher.setAttributes(register.getAttributes());
    publisher.setClientVersion(ClientVersion.StoreData);

    DataInfo dataInfo =
        new DataInfo(register.getInstanceId(), register.getDataId(), register.getGroup());
    publisher.setDataInfoId(dataInfo.getDataInfoId());
  }

  /**
   * PublisherRegister to Publisher
   *
   * @param
   * @return
   */
  public static Publisher convert(PublisherRegister publisherRegister, Channel channel) {
    return PUBLISHER_CONVERTER.convert(new Tuple<>(publisherRegister, channel));
  }

  public static List<ServerDataBox> convert(List<DataBox> boxList) {
    List<ServerDataBox> serverDataBoxes = new ArrayList<>();
    if (null != boxList) {
      for (DataBox dataBox : boxList) {
        ServerDataBox serverDataBox = new ServerDataBox(ServerDataBox.getBytes(dataBox.getData()));
        serverDataBoxes.add(serverDataBox);
      }
    }
    return serverDataBoxes;
  }
}
