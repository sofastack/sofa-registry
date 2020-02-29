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
package com.alipay.sofa.registry.server.session.store;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.ScopeEnum;

/**
 *
 * @author shangyu.wh
 * @version $Id: SessionInterests.java, v 0.1 2017-11-30 15:53 shangyu.wh Exp $
 */
public interface Interests extends DataManager<Subscriber, String, String> {

    /**
     * query subscribers by dataInfoID
     *
     * @param dataInfoId
     * @return
     */
    Collection<Subscriber> getInterests(String dataInfoId);

    /**
     * check subscribers interest dataInfoId version,very dataCenter dataInfoId version different
     * if return false
     * else check return bigger version
     *
     * @param dataCenter
     * @param dataInfoId
     * @param version
     * @return
     */
    boolean checkInterestVersions(String dataCenter, String dataInfoId, Long version);

    /**
     * check subscribers interest dataInfoId version,very dataCenter dataInfoId version different
     * if not exist add
     * else check and update big version
     *
     * @param dataCenter
     * @param dataInfoId
     * @param version
     * @return
     */
    boolean checkAndUpdateInterestVersions(String dataCenter, String dataInfoId, Long version);

    /**
     * set subscribers interest dataInfoId version zero
     * @param dataCenter
     * @param dataInfoId
     * @return
     */
    boolean checkAndUpdateInterestVersionZero(String dataCenter, String dataInfoId);

    /**
     * get all subscriber dataInfoIds
     *
     * @return
     */
    Collection<String> getInterestDataInfoIds();

    /**
     * get subscribers whith specify dataInfo and scope,and group by source InetSocketAddress
     * @param dataInfoId
     * @param scope
     * @return
     */
    Map<InetSocketAddress, Map<String, Subscriber>> querySubscriberIndex(String dataInfoId,
                                                                         ScopeEnum scope);

    /**
     * get subscriber by dataInfoId and registerId
     * @param registerId
     * @param dataInfoId
     * @return
     */
    Subscriber queryById(String registerId, String dataInfoId);

    /**
     * get all subscribers group by connectId
     * @return
     */
    Map<String/*connectId*/, Map<String/*registerId*/, Subscriber>> getConnectSubscribers();

    List<String> getDataCenters();
}