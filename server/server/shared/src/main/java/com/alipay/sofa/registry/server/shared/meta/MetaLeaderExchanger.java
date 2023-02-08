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
package com.alipay.sofa.registry.server.shared.meta;

import com.alipay.sofa.registry.common.model.elector.LeaderInfo;
import com.alipay.sofa.registry.remoting.exchange.RequestException;
import com.alipay.sofa.registry.remoting.exchange.message.Response;

/**
 * @author xiaojian.xj
 * @version : MetaLeaderExchanger.java, v 0.1 2022年04月16日 15:54 xiaojian.xj Exp $
 */
public interface MetaLeaderExchanger {

  /**
   * send request to remote cluster meta leader
   *
   * @param dataCenter
   * @param requestBody
   * @return
   * @throws RequestException
   */
  Response sendRequest(String dataCenter, Object requestBody) throws RequestException;

  /**
   * learn leader from remote resp
   *
   * @param dataCenter
   * @param leaderInfo
   */
  boolean learn(String dataCenter, LeaderInfo leaderInfo);

  /**
   * reset leader from remoteMetaDomain
   *
   * @param dataCenter
   */
  LeaderInfo resetLeader(String dataCenter);

  /**
   * get leader info
   *
   * @param dataCenter
   * @return
   */
  LeaderInfo getLeader(String dataCenter);

  /**
   * remove leader
   *
   * @param dataCenter
   */
  void removeLeader(String dataCenter);
}
