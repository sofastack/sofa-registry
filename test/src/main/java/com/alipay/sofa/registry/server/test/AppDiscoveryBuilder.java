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

import com.alipay.sofa.registry.common.model.AppRegisterServerDataBox;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.core.model.AppRevisionRegister;

import java.util.*;

public class AppDiscoveryBuilder {
    private AppRevisionRegister      revisionRegister = new AppRevisionRegister();
    private AppRegisterServerDataBox dataBox          = new AppRegisterServerDataBox();

    public AppDiscoveryBuilder(String appname, String revision, String address) {
        dataBox.setUrl(address);
        dataBox.setRevision(revision);
        revisionRegister.setAppName(appname);
        revisionRegister.setRevision(revision);
    }

    public String addService(String serviceName, String group, String instanceID) {
        if(revisionRegister.getInterfaces()==null){
            revisionRegister.setInterfaces(new HashMap<>());
        }
        String serviceId = DataInfo.toDataInfoId(serviceName, instanceID, group);
        AppRevisionInterface inf =revisionRegister.getInterfaces().computeIfAbsent(DataInfo.toDataInfoId(serviceName, instanceID, group), k->new AppRevisionInterface());
        inf.setGroup(group);
        inf.setInstanceId(instanceID);
        inf.setDataId(serviceName);
        return serviceId;
    }

    public void addMetaInterfaceParam(String serviceId, String key, String value){
        AppRevisionInterface inf =revisionRegister.getInterfaces().get(serviceId);
        if(inf.getServiceParams()== null){
            inf.setServiceParams(new HashMap<>());
        }
        inf.getServiceParams().computeIfAbsent(key, k->new ArrayList<>()).add(value);
    }

    public void addMetaBaseParam(String key, String value){
        if(revisionRegister.getBaseParams()== null){
            revisionRegister.setBaseParams(new HashMap<>());
        }
        revisionRegister.getBaseParams().computeIfAbsent(key, k->new ArrayList<>()).add(value);
    }

    public void addDataBaseParam(String key, String value){
        if(dataBox.getBaseParams() == null){
            dataBox.setBaseParams(new HashMap<>());
        }
        dataBox.getBaseParams().computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    public void addDataInterfaceParam(String serviceId, String key, String value){
        if(dataBox.getInterfaceParams()==null){
            dataBox.setInterfaceParams(new HashMap<>());
        }
        dataBox.getInterfaceParams().computeIfAbsent(serviceId, k -> new HashMap<>()).computeIfAbsent(key, k->new ArrayList<>()).add(value);
    }

    public AppRevisionRegister buildAppRevision() {
        return revisionRegister;
    }

    public AppRegisterServerDataBox buildData() {
        return dataBox;
    }
}
