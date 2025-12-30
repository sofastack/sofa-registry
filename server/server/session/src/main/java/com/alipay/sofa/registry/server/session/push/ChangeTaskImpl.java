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
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.util.StringFormatter;
import java.util.Objects;

public final class ChangeTaskImpl implements ChangeTask<ChangeKey> {
  final TriggerPushContext changeCtx;
  final ChangeKey key;
  final ChangeHandler changeHandler;
  final long expireTimestamp;
  long expireDeadlineTimestamp;

  ChangeTaskImpl(
      ChangeKey key,
      TriggerPushContext changeCtx,
      ChangeHandler changeHandler,
      long expireTimestamp) {
    this.key = key;
    this.changeHandler = changeHandler;
    this.changeCtx = changeCtx;
    this.expireTimestamp = expireTimestamp;
  }

  void doChange() {
    changeHandler.onChange(key.dataInfoId, changeCtx);
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "ChangeTask{{},ver={},expire={},deadline={}}",
        key,
        changeCtx.getExpectDatumVersion(),
        expireTimestamp,
        expireDeadlineTimestamp);
  }

  @Override
  public ChangeKey key() {
    return this.key;
  }

  @Override
  public long deadline() {
    return this.expireTimestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return false;
    }

    if (!(o instanceof ChangeTaskImpl)) {
      return false;
    }

    ChangeTaskImpl other = (ChangeTaskImpl) o;
    return Objects.equals(this.key, other.key);
  }
}
