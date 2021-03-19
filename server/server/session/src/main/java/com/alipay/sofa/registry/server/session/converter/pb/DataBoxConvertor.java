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

import com.alipay.sofa.registry.common.model.client.pb.DataBoxPb;
import com.alipay.sofa.registry.common.model.client.pb.DataBoxesPb;
import com.alipay.sofa.registry.core.model.DataBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author bystander
 * @version $Id: DataBoxConvertor.java, v 0.1 2018年03月21日 2:19 PM bystander Exp $
 */
public class DataBoxConvertor {

  public static DataBox convert2Java(DataBoxPb dataBoxPb) {

    if (dataBoxPb == null) {
      return null;
    } else {
      DataBox dataBoxJava = new DataBox();
      dataBoxJava.setData(dataBoxPb.getData());
      return dataBoxJava;
    }
  }

  public static DataBoxPb convert2Pb(DataBox dataBoxJava) {

    if (dataBoxJava == null) {
      return null;
    } else {
      DataBoxPb.Builder builder = DataBoxPb.newBuilder();

      if (dataBoxJava.getData() != null) {
        builder.setData(dataBoxJava.getData());
      }
      return builder.build();
    }
  }

  public static List<DataBox> convertBoxes2Javas(List<DataBoxPb> dataBoxPbs) {

    List<DataBox> result = new ArrayList<>();
    if (dataBoxPbs == null) {
      return null;
    } else {

      for (DataBoxPb dataBoxPb : dataBoxPbs) {
        result.add(convert2Java(dataBoxPb));
      }

      return result;
    }
  }

  public static List<DataBoxPb> convert2Pbs(List<DataBox> dataBoxJavas) {

    List<DataBoxPb> result = new ArrayList<>();
    if (dataBoxJavas == null) {
      return null;
    } else {

      for (DataBox dataBoxJava : dataBoxJavas) {
        result.add(convert2Pb(dataBoxJava));
      }

      return result;
    }
  }

  public static List<DataBox> convertBoxes2Javas(DataBoxesPb dataBoxesPb) {
    if (dataBoxesPb == null) {
      return null;
    } else {

      return convertBoxes2Javas(dataBoxesPb.getDataList());
    }
  }

  public static Map<String, List<DataBox>> convert2JavaMaps(Map<String, DataBoxesPb> mapPb) {
    if (mapPb == null) {
      return null;
    } else {
      Map<String, List<DataBox>> mapJava = new HashMap<>();

      for (Entry<String, DataBoxesPb> entry : mapPb.entrySet()) {
        mapJava.put(entry.getKey(), convertBoxes2Javas(entry.getValue()));
      }

      return mapJava;
    }
  }

  private static DataBoxesPb convertBoxes2Pbs(List<DataBox> dataBoxes) {
    if (dataBoxes == null) {
      return null;
    }
    return DataBoxesPb.newBuilder().addAllData(convert2Pbs(dataBoxes)).build();
  }

  public static Map<String, DataBoxesPb> convert2PbMaps(Map<String, List<DataBox>> mapJava) {
    if (null == mapJava) {
      return null;
    }

    Map<String, DataBoxesPb> mapPb = new HashMap<>();

    for (Entry<String, List<DataBox>> entry : mapJava.entrySet()) {
      mapPb.put(entry.getKey(), convertBoxes2Pbs(entry.getValue()));
    }
    return mapPb;
  }
}
