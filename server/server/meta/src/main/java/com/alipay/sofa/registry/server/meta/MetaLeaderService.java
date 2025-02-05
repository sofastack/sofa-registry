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
package com.alipay.sofa.registry.server.meta;

import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector.LeaderInfo;

/**
 * @author chen.zhu
 *     <p>Mar 10, 2021
 */
public interface MetaLeaderService {

  /**
   * Am i elector boolean.
   *
   * @return the boolean
   */
  boolean amILeader();

  /**
   * Gets get elector.
   *
   * @return the get elector
   */
  String getLeader();

  /**
   * Gets get elector epoch.
   *
   * @return the get elector epoch
   */
  long getLeaderEpoch();

  /**
   * get leader info
   *
   * @return LeaderInfo
   */
  LeaderInfo getLeaderInfo();

  /**
   * Gets get meta server elector state.
   *
   * @return the get meta server elector state
   */
  boolean isWarmuped();

  /**
   * Am i stable as elector boolean.
   *
   * @return the boolean
   */
  default boolean amIStableAsLeader() {
    return amILeader() && isWarmuped();
  }

  void registerListener(MetaLeaderElectorListener listener);

  interface MetaLeaderElectorListener {

    void becomeLeader();

    void loseLeader();
  }
}
