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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.common.model.ProcessId;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

public final class ProcessIdCache {
  private static final Interner<ProcessId> interners = Interners.newWeakInterner();

  private ProcessIdCache() {}

  public static ProcessId cache(ProcessId pid) {
    if (pid == null) {
      return null;
    }
    return interners.intern(pid);
  }
}
