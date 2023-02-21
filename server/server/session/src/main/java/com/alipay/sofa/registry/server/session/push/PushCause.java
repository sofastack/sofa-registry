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
import java.util.Map;

public final class PushCause {
  final TriggerPushContext triggerPushCtx;
  final Map<String, Long> datumTimestamp;
  final PushType pushType;

  PushCause(
      TriggerPushContext triggerPushCtx, PushType pushType, Map<String, Long> datumTimestamp) {
    this.pushType = pushType;
    this.datumTimestamp = datumTimestamp;
    this.triggerPushCtx = triggerPushCtx;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "PushCause{{},datumTs={},triggerPushCtx={}}", pushType, datumTimestamp, triggerPushCtx);
  }
}
