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
package com.alipay.sofa.registry.server.session.renew;

import java.util.List;

import com.alipay.sofa.registry.common.model.DatumSnapshotRequest;
import com.alipay.sofa.registry.common.model.RenewDatumRequest;

/**
 *
 * @author kezhu.wukz
 * @version $Id: RenewService.java, v 0.1 2019-06-27 11:06 kezhu.wukz Exp $
 */
public interface RenewService {

    /**
     * Get RenewDatumRequests based on connectId
     *
     * @param connectId
     * @return
     */
    List<RenewDatumRequest> getRenewDatumRequests(String connectId);

    /**
     * Get DatumSnapshotRequests based on connectId
     *
     * @param connectId
     * @return
     */
    List<DatumSnapshotRequest> getDatumSnapshotRequest(String connectId);

    DatumSnapshotRequest getDatumSnapshotRequest(String connectId, String dataServerIP);
}