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
package com.alipay.sofa.registry.common.model.sessionserver;

import com.alipay.sofa.registry.util.StringFormatter;
import java.io.Serializable;

public final class SimpleSubscriber implements Serializable {
  private static final long serialVersionUID = -1320693859225668853L;

  private final String clientId;
  private final String sourceAddress;
  private final String appName;

  public SimpleSubscriber(String clientId, String sourceAddress, String appName) {
    this.clientId = clientId;
    this.sourceAddress = sourceAddress;
    this.appName = appName;
  }

  public String getClientId() {
    return clientId;
  }

  public String getSourceAddress() {
    return sourceAddress;
  }

  public String getAppName() {
    return appName;
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "SimpleSubscriber{app={},clientId={},add={}}", appName, clientId, sourceAddress);
  }
}
