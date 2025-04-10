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

import com.alipay.sofa.registry.common.model.dataserver.DatumRevisionMark;
import java.io.Serializable;

/**
 * @author huicha
 * @date 2025/5/16
 */
public class SubDatumRevisionMark implements Serializable {

  private static final long serialVersionUID = -5991371918392923434L;

  public static SubDatumRevisionMark from(DatumRevisionMark datumRevisionMark) {
    return new SubDatumRevisionMark(datumRevisionMark.getVersion(), datumRevisionMark.isMock());
  }

  public static SubDatumRevisionMark of(long version, boolean mock) {
    return new SubDatumRevisionMark(version, mock);
  }

  private long version;

  private boolean mock;

  public SubDatumRevisionMark(long version, boolean mock) {
    this.version = version;
    this.mock = mock;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public boolean isMock() {
    return mock;
  }

  public void setMock(boolean mock) {
    this.mock = mock;
  }
}
