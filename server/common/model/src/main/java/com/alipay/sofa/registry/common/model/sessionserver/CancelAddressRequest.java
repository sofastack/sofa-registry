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

import com.alipay.sofa.registry.common.model.ConnectId;
import java.io.Serializable;
import java.util.List;

/**
 * The type Cancel address request.
 *
 * @author shangyu.wh
 * @version $Id : CancelAddressRequest.java, v 0.1 2017-12-22 17:04 shangyu.wh Exp $
 */
public class CancelAddressRequest implements Serializable {

  private static final long serialVersionUID = -4398310292728124256L;

  private List<ConnectId> connectIds;

  // the bean used in http facade, need no-arg construct
  public CancelAddressRequest() {}

  /**
   * Constructor.
   *
   * @param connectIds the connect ids
   */
  public CancelAddressRequest(List<ConnectId> connectIds) {
    this.connectIds = connectIds;
  }

  /**
   * Getter method for property <tt>connectIds</tt>.
   *
   * @return property value of connectIds
   */
  public List<ConnectId> getConnectIds() {
    return connectIds;
  }

  public void setConnectIds(List<ConnectId> connectIds) {
    this.connectIds = connectIds;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CancelAddressRequest{");
    sb.append("connectIds=").append(connectIds);
    sb.append('}');
    return sb.toString();
  }
}
