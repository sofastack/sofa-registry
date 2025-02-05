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
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shangyu.wh
 * @version $Id: PublisherConvert.java, v 0.1 2017-11-30 17:54 shangyu.wh Exp $
 */
public final class PublisherConverter {
  private PublisherConverter() {}

  private static final Converter<PublisherRegister, Publisher> publisherConverter =
      source -> {
        Publisher publisher = new Publisher();

        fillCommonRegion(publisher, source);
        publisher.setDataList(convert(source.getDataList()));

        return publisher;
      };

  public static void fillCommonRegion(Publisher publisher, PublisherRegister source) {
    publisher.setAppName(source.getAppName());
    // ZONE MUST BE CURRENT SESSION ZONE
    publisher.setCell(source.getZone());
    publisher.setClientId(source.getClientId());
    publisher.setDataId(source.getDataId());
    publisher.setGroup(source.getGroup());
    publisher.setInstanceId(source.getInstanceId());
    publisher.setRegisterId(source.getRegistId());
    publisher.setProcessId(source.getProcessId());
    if (source.getVersion() != null) {
      publisher.setVersion(source.getVersion());
    }

    // registerTimestamp must happen from server,client time maybe different cause pub and
    // unPublisher fail
    publisher.setRegisterTimestamp(System.currentTimeMillis());

    publisher.setClientRegisterTimestamp(source.getTimestamp());
    publisher.setSourceAddress(new URL(source.getIp(), source.getPort()));
    publisher.setAttributes(source.getAttributes());
    publisher.setClientVersion(ClientVersion.StoreData);

    DataInfo dataInfo = new DataInfo(source.getInstanceId(), source.getDataId(), source.getGroup());
    publisher.setDataInfoId(dataInfo.getDataInfoId());
  }

  /**
   * PublisherRegister to Publisher
   *
   * @param publisherRegister publisherRegister
   * @return Publisher
   */
  public static Publisher convert(PublisherRegister publisherRegister) {
    return publisherConverter.convert(publisherRegister);
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
