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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;
import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.store.AppPublisher;
import com.alipay.sofa.registry.common.model.store.BaseInfo.ClientVersion;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.DataBox;
import com.alipay.sofa.registry.core.model.PublisherRegister;
import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author shangyu.wh
 * @version $Id: PublisherConvert.java, v 0.1 2017-11-30 17:54 shangyu.wh Exp $
 */
public class PublisherConverter {


    public static final String PUB_TYPE = "!PublisherType";

    public static final String APP_PUBLISHER = "APP_PUBLISHER";

    private static Converter<PublisherRegister, AppPublisher> appPublisherConverter = source -> {
        AppPublisher appPublisher = new AppPublisher();
        fillCommonRegion(appPublisher, source);
        appPublisher.setAppDataList(convert2AppDataList(source.getDataList()));

        return appPublisher;
    };

    private static Converter<PublisherRegister, Publisher> publisherConverter = source -> {
        Publisher publisher = new Publisher();

        fillCommonRegion(publisher, source);
        publisher.setDataList(convert(source.getDataList()));

        return publisher;
    };


    public static void fillCommonRegion(Publisher publisher, PublisherRegister source) {
        publisher.setAppName(source.getAppName());
        //ZONE MUST BE CURRENT SESSION ZONE
        publisher.setCell(source.getZone());
        publisher.setClientId(source.getClientId());
        publisher.setDataId(source.getDataId());
        publisher.setGroup(source.getGroup());
        publisher.setInstanceId(source.getInstanceId());
        publisher.setRegisterId(source.getRegistId());
        publisher.setProcessId(source.getProcessId());
        publisher.setVersion(source.getVersion());

        //registerTimestamp must happen from server,client time maybe different cause pub and unPublisher fail
        publisher.setRegisterTimestamp(System.currentTimeMillis());

        publisher.setClientRegisterTimestamp(source.getTimestamp());
        publisher.setSourceAddress(new URL(source.getIp(), source.getPort()));

        publisher.setClientVersion(ClientVersion.StoreData);

        DataInfo dataInfo = new DataInfo(source.getInstanceId(), source.getDataId(),
                source.getGroup());
        publisher.setDataInfoId(dataInfo.getDataInfoId());
    }

    /**
     * PublisherRegister to Publisher
     *
     * @param publisherRegister
     * @return
     */
    public static Publisher convert(PublisherRegister publisherRegister) {

        if (StringUtils.equalsIgnoreCase(APP_PUBLISHER, publisherRegister.getAttributes().get(PUB_TYPE))) {
            return appPublisherConverter.convert(publisherRegister);
        }

        return publisherConverter.convert(publisherRegister);
    }

    public static List<ServerDataBox> convert(List<DataBox> boxList) {
        List<ServerDataBox> serverDataBoxes = new ArrayList<>();
        if (null != boxList) {
            for (DataBox dataBox : boxList) {
                ServerDataBox serverDataBox = new ServerDataBox(ServerDataBox.getBytes(dataBox
                    .getData()));
                serverDataBoxes.add(serverDataBox);
            }
        }
        return serverDataBoxes;
    }

    private static List<AppRegisterServerDataBox> convert2AppDataList(List<DataBox> dataList) {
        List<AppRegisterServerDataBox> dataBoxes = new ArrayList<>();
        if (CollectionUtils.isEmpty(dataList)) {
            return dataBoxes;
        }

        for (DataBox dataBox : dataList) {
            AppRegisterServerDataBox serverDataBox = new AppRegisterServerDataBox();
            JSONObject jsonObject = JSON.parseObject(dataBox.getData());
            serverDataBox.setUrl(jsonObject.getString(AppRegisterConstant.URL_KEY));
            serverDataBox.setRevision(jsonObject.getString(AppRegisterConstant.REVISION_KEY));
            serverDataBox.setBaseParams(JSONObject.parseObject(jsonObject.getString(AppRegisterConstant.BASE_PARAMS_KEY), HashMap.class));
            serverDataBox.setServiceParams(JSONObject.parseObject(jsonObject.getString(AppRegisterConstant.INTERFACE_PARAMS_KEY), HashMap.class));
            dataBoxes.add(serverDataBox);
        }

        return dataBoxes;
    }
}