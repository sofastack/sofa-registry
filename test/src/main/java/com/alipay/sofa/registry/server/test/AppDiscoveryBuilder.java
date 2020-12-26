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
package com.alipay.sofa.registry.server.test;

import com.alipay.sofa.registry.core.model.AppPublisherBody;
import com.alipay.sofa.registry.core.model.AppPublisherBodyInterface;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;

import java.util.*;

public class AppDiscoveryBuilder {
    private AppRevisionRegister revisionRegister = new AppRevisionRegister();
    private AppPublisherBody    publisherBody    = new AppPublisherBody();

    public AppDiscoveryBuilder(String appname, String revision, String address) {
        publisherBody.setUrl(address);
        publisherBody.setRevision(revision);
        publisherBody.setBaseParams(new HashMap<>());
        publisherBody.setInterfaces(new ArrayList<>());
        revisionRegister.setAppName(appname);
        revisionRegister.setRevision(revision);
        revisionRegister.setBaseParams(new HashMap<>());
        revisionRegister.setInterfaces(new ArrayList<>());
    }

    public AppRevisionInterface addService(String serviceName, String group, String instanceID) {
        AppRevisionInterface inf = new AppRevisionInterface();
        inf.setGroup(group);
        inf.setInstanceId(instanceID);
        inf.setDataId(serviceName);
        revisionRegister.getInterfaceList().add(inf);
        return inf;
    }

    public void addMetaInterfaceParam(AppRevisionInterface inf, String key, String value) {
        if (inf.getServiceParams() == null) {
            inf.setServiceParams(new HashMap<>());
        }
        inf.getServiceParams().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addMetaBaseParam(String key, String value) {
        revisionRegister.getBaseParams().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addDataBaseParam(String key, String value) {
        publisherBody.getBaseParams().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addDataInterfaceParam(AppRevisionInterface inf, String key, String value) {
        AppPublisherBodyInterface pubInf = null;
        for (AppPublisherBodyInterface tmpPubInf : publisherBody.getInterfaces()) {
            if (tmpPubInf.getDataId().equals(inf.getDataId()) &&
                    tmpPubInf.getGroup().equals(inf.getGroup()) &&
                    tmpPubInf.getInstanceId().equals(inf.getInstanceId())) {
                pubInf = tmpPubInf;
                break;
            }
        }
        if (pubInf == null) {
            pubInf = new AppPublisherBodyInterface();
            pubInf.setDataId(inf.getDataId());
            pubInf.setInstanceId(inf.getInstanceId());
            pubInf.setGroup(inf.getGroup());
            pubInf.setParams(new HashMap<>());
            publisherBody.getInterfaces().add(pubInf);
        }
        pubInf.getParams().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public AppRevisionRegister buildAppRevision() {
        return revisionRegister;
    }

    public AppPublisherBody buildData() {
        return publisherBody;
    }
}
