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

import com.alipay.sofa.registry.common.model.client.pb.ReceivedConfigDataPb;
import com.alipay.sofa.registry.common.model.client.pb.ReceivedDataPb;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;

/**
 * @author bystander
 * @version $Id: ReceivedDataConvertor.java, v 0.1 2018年03月21日 2:07 PM bystander
 *          Exp $
 */
public class ReceivedDataConvertor {

	public static ReceivedData convert2Java(ReceivedDataPb receivedDataPb) {

		if (receivedDataPb == null) {
			return null;
		}

		ReceivedData receivedData = new ReceivedData();

		receivedData.setData(DataBoxConvertor.convert2JavaMaps(receivedDataPb
				.getDataMap()));
		receivedData.setDataId(receivedDataPb.getDataId());
		receivedData.setGroup(receivedDataPb.getGroup());
		receivedData.setInstanceId(receivedDataPb.getInstanceId());
		receivedData.setLocalZone(receivedDataPb.getLocalZone());
		receivedData.setScope(receivedDataPb.getScope());
		receivedData.setSegment(receivedDataPb.getSegment());
		receivedData.setSubscriberRegistIds(ListStringConvertor
				.convert2Java(receivedDataPb.getSubscriberRegistIdsList()));
		receivedData.setVersion(receivedDataPb.getVersion());

		return receivedData;
	}

	public static ReceivedDataPb convert2Pb(ReceivedData receivedDataJava) {

		if (receivedDataJava == null) {
			return null;
		}

		ReceivedDataPb.Builder builder = ReceivedDataPb.newBuilder();

		builder.setDataId(receivedDataJava.getDataId())
				.setGroup(receivedDataJava.getGroup())
				.setInstanceId(receivedDataJava.getInstanceId())
				.setLocalZone(receivedDataJava.getLocalZone())
				.setScope(receivedDataJava.getScope())
				.setSegment(receivedDataJava.getSegment())
				.setVersion(receivedDataJava.getVersion())
				.addAllSubscriberRegistIds(
						receivedDataJava.getSubscriberRegistIds())
				.putAllData(
						DataBoxConvertor.convert2PbMaps(receivedDataJava
								.getData()));

		return builder.build();

	}

	public static ReceivedConfigDataPb convert2Pb(
			ReceivedConfigData receivedConfigData) {

		if (receivedConfigData == null) {
			return null;
		}

		ReceivedConfigDataPb.Builder builder = ReceivedConfigDataPb
				.newBuilder();

		builder.setDataId(receivedConfigData.getDataId())
				.setGroup(receivedConfigData.getGroup())
				.setInstanceId(receivedConfigData.getInstanceId())
				.setVersion(receivedConfigData.getVersion())
				.addAllConfiguratorRegistIds(
						receivedConfigData.getConfiguratorRegistIds())
				.setDataBox(
						DataBoxConvertor.convert2Pb(receivedConfigData
								.getDataBox()));

		return builder.build();

	}
}