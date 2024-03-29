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
package com.alipay.sofa.registry.common.model.metaserver;

/**
 * @author xiaojian.xj
 * @version : ClientManagerResult.java, v 0.1 2022年01月20日 14:16 xiaojian.xj Exp $
 */
public class ClientManagerResult {

  public static final long FAIL_VERSION = -1L;

  private final boolean success;

  private final long version;

  public ClientManagerResult(boolean success, long version) {
    this.success = success;
    this.version = version;
  }

  public static ClientManagerResult buildSuccess(long version) {
    return new ClientManagerResult(true, version);
  }

  public static ClientManagerResult buildFailRet() {
    return new ClientManagerResult(false, FAIL_VERSION);
  }

  public boolean isSuccess() {
    return success;
  }
  /**
   * Getter method for property <tt>version</tt>.
   *
   * @return property value of version
   */
  public long getVersion() {
    return version;
  }

  @Override
  public String toString() {
    return "ClientManagerResult{" + "success=" + success + ", version=" + version + '}';
  }
}
