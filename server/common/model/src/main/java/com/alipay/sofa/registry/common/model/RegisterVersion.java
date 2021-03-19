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
package com.alipay.sofa.registry.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-12-02 21:44 yuzhi.lyz Exp $
 */
public final class RegisterVersion implements Serializable, Comparable<RegisterVersion> {
  private final long version;
  private final long registerTimestamp;

  public RegisterVersion(long version, long registerTimestamp) {
    this.version = version;
    this.registerTimestamp = registerTimestamp;
  }

  public static RegisterVersion of(long version, long registerTimestamp) {
    return new RegisterVersion(version, registerTimestamp);
  }

  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Getter method for property <tt>registerTimestamp</tt>.
   *
   * @return property value of registerTimestamp
   */
  public long getRegisterTimestamp() {
    return registerTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RegisterVersion)) return false;
    RegisterVersion that = (RegisterVersion) o;
    return version == that.version && registerTimestamp == that.registerTimestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, registerTimestamp);
  }

  @Override
  public int compareTo(RegisterVersion o) {
    if (version < o.version) {
      return -1;
    }
    if (version > o.version) {
      return 1;
    }

    if (registerTimestamp < o.registerTimestamp) {
      return -1;
    }
    if (registerTimestamp > o.registerTimestamp) {
      return 1;
    }
    return 0;
  }

  public boolean orderThan(RegisterVersion o) {
    return compareTo(o) < 0;
  }

  @Override
  public String toString() {
    return "RegisterVersion{"
        + "version="
        + version
        + ", registerTimestamp="
        + registerTimestamp
        + '}';
  }

  public RegisterVersion incrRegisterTimestamp() {
    return RegisterVersion.of(this.version, this.registerTimestamp + 1);
  }
}
