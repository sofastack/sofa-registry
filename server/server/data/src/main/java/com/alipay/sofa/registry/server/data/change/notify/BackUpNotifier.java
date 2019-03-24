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
package com.alipay.sofa.registry.server.data.change.notify;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.datasync.Operator;
import com.alipay.sofa.registry.server.data.datasync.SyncDataService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author qian.lqlq
 * @version $Id: BackUpNotifier.java, v 0.1 2018-03-12 18:40 qian.lqlq Exp $
 */
public class BackUpNotifier implements IDataChangeNotifier {

    @Autowired
    private SyncDataService syncDataService;

    @Override
    public Set<DataSourceTypeEnum> getSuitableSource() {
        Set<DataSourceTypeEnum> set = new HashSet<>();
        set.add(DataSourceTypeEnum.PUB);
        set.add(DataSourceTypeEnum.SYNC);
        return set;
    }

    @Override
    public void notify(Datum datum, Long lastVersion) {
        syncDataService.appendOperator(new Operator(datum.getVersion(), lastVersion, datum,
            DataSourceTypeEnum.BACKUP));
    }
}