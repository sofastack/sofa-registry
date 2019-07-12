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
package com.alipay.sofa.registry.server.data.datasync;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;

/**
 *
 * @author kezhu.wukz
 * @version $Id: SnapshotOperator.java, v 0.1 2019-07-12 20:33 kezhu.wukz Exp $
 */
public class SnapshotOperator extends Operator {

    public SnapshotOperator(Long version, Long sourceVersion, Datum datum,
                            DataSourceTypeEnum sourceType) {
        super(version, sourceVersion, datum, sourceType);
    }
}