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
package com.alipay.sofa.registry.server.session.loggers;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

public final class Loggers {
  private Loggers() {}

  public static final Logger CLIENT_DISABLE_LOG = LoggerFactory.getLogger("CLIENT-DISABLE");
  public static final Logger CLIENT_OFF_LOG = LoggerFactory.getLogger("CLIENT-OFF");
  public static final Logger BLACK_LIST_LOG = LoggerFactory.getLogger("BLACK-LIST");
}
