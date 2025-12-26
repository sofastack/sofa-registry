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
package com.alipay.sofa.registry.server.data.cache;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author huicha
 * @date 2025/10/30
 */
public class NotThreadSafePublisherMap {

  private final AtomicInteger realPubNum;

  private final Map<String, PublisherEnvelope> publishers;

  public NotThreadSafePublisherMap() {
    this.realPubNum = new AtomicInteger(0);
    this.publishers = Maps.newConcurrentMap();
  }

  // read method
  public int size() {
    return this.publishers.size();
  }

  public Collection<PublisherEnvelope> values() {
    return this.publishers.values();
  }

  public PublisherEnvelope get(String registerId) {
    return this.publishers.get(registerId);
  }

  public Set<Map.Entry<String, PublisherEnvelope>> entrySet() {
    return this.publishers.entrySet();
  }

  public void forEach(BiConsumer<String, PublisherEnvelope> action) {
    this.publishers.forEach(action);
  }

  public int getRealPubNum() {
    return this.realPubNum.get();
  }

  // write method, need update real publisher number
  public PublisherEnvelope put(String registerId, PublisherEnvelope publisher) {
    if (null == publisher) {
      return null;
    }

    PublisherEnvelope existPublisher = this.publishers.put(registerId, publisher);
    if (null == existPublisher) {
      if (publisher.isPub()) {
        this.realPubNum.incrementAndGet();
      }
      return null;
    }

    boolean isExistPub = existPublisher.isPub();
    boolean isPub = publisher.isPub();

    // 下面的逻辑是这段代码的简化
    //    if (isExistPub) {
    //      if (isPub) {
    //        // Pub -> Pub: 没变化
    //      } else {
    //        // Pub -> Unpub: Publisher 数量减少
    //        this.realPubNum--;
    //      }
    //    } else {
    //      if (isPub) {
    //        // Unpub -> Pub: Publisher 数量增加
    //        this.realPubNum++;
    //      } else {
    //        // Unpub -> Unpub: 没变化
    //      }
    //    }
    if (isExistPub && !isPub) {
      this.realPubNum.decrementAndGet();
    } else if (!isExistPub && isPub) {
      this.realPubNum.incrementAndGet();
    }

    return existPublisher;
  }

  public void clear() {
    this.publishers.clear();
    this.realPubNum.set(0);
  }

  public PublisherEnvelope remove(String registerId) {
    PublisherEnvelope existPublisher = this.publishers.remove(registerId);
    if (null != existPublisher && existPublisher.isPub()) {
      // 移除了 Pub，那么 Publisher 数量减少
      this.realPubNum.decrementAndGet();
    }
    return existPublisher;
  }

  public boolean remove(String registerId, PublisherEnvelope expectDeletePublisher) {
    boolean deleted = this.publishers.remove(registerId, expectDeletePublisher);
    if (deleted) {
      // 成功删除了
      if (expectDeletePublisher.isPub()) {
        // 删除了一个 Publisher，计数需要减少
        this.realPubNum.decrementAndGet();
      }
    }
    return deleted;
  }
}
