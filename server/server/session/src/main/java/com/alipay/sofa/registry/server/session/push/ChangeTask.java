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

import java.util.Objects;

/**
 * @author huicha
 * @date 2025/10/27
 */
public interface ChangeTask<Key extends Comparable<Key>> extends Comparable<ChangeTask<Key>> {

  Key key();

  long deadline();

  @Override
  default int compareTo(ChangeTask<Key> o) {
    Key key = this.key();
    Key otherKey = o.key();

    if (Objects.equals(key, otherKey)) {
      return 0;
    }

    long deadline = this.deadline();
    long otherDeadline = o.deadline();
    if (deadline == otherDeadline) {
      return key.compareTo(otherKey);
    }
    return deadline < otherDeadline ? -1 : 1;
  }
}
