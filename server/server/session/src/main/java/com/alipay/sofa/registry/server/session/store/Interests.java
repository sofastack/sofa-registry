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

import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.registry.SessionRegistry.SelectSubscriber;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shangyu.wh
 * @version $Id: SessionInterests.java, v 0.1 2017-11-30 15:53 shangyu.wh Exp $
 */
public interface Interests extends DataManager<Subscriber, String, String> {

  /**
   * check subscribers interest dataInfoId version,very dataCenter dataInfoId version different if
   * return false else check return bigger version
   *
   * @param dataCenter dataCenter
   * @param datumDataInfoId datumDataInfoId
   * @param version version
   * @return InterestVersionCheck
   */
  InterestVersionCheck checkInterestVersion(
      String dataCenter, String datumDataInfoId, long version);

  Collection<Subscriber> getInterests(String datumDataInfoId);

  SelectSubscriber selectSubscribers(Set<String> dataCenters);

  Map<String, List<String>> filterIPs(String group, int limit);

  enum InterestVersionCheck {
    NoSub(false),
    Obsolete(false),
    Interested(true),
    ;
    public final boolean interested;

    InterestVersionCheck(boolean Interested) {
      this.interested = Interested;
    }
  }
}
