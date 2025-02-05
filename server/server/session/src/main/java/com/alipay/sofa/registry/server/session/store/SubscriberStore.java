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
import com.alipay.sofa.registry.server.session.registry.SessionRegistry;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Used to store subscriber information. */
public interface SubscriberStore extends ClientStore<Subscriber> {

  /**
   * Group subscriber by group and returns a limit of elements.
   *
   * @param group target group of Subscriber
   * @param limit maximum number to return, returns empty if negative
   * @return subscriber collection
   */
  Map<String, Collection<Subscriber>> query(String group, int limit);

  /**
   * Select subscriber with target data center.
   *
   * @param dataCenter target data center
   * @return subscriber collection
   */
  //  Tuple<Map<String, DatumVersion>, List<Subscriber>> selectSubscribers(String dataCenter);

  SessionRegistry.SelectSubscriber selectSubscribers(Set<String> dataCenters);

  /**
   * Check if there is subscriber that interest the data and has correct version.
   *
   * @param dataCenter data center
   * @param datumDataInfoId data id
   * @param version required version
   * @return InterestVersionCheck
   */
  InterestVersionCheck checkInterestVersion(
      String dataCenter, String datumDataInfoId, long version);

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
