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
package com.alipay.sofa.registry.server.session.acceptor;

import com.alipay.sofa.registry.common.model.ConnectId;
import com.alipay.sofa.registry.common.model.store.Publisher;

/** WriteDataRequest of publisher register. */
public final class PublisherRegisterWriteDataRequest implements WriteDataRequest<Publisher> {

  private final Publisher publisher;

  public PublisherRegisterWriteDataRequest(Publisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public Publisher getRequestBody() {
    return publisher;
  }

  @Override
  public WriteDataRequestType getRequestType() {
    return WriteDataRequestType.PUBLISHER;
  }

  @Override
  public ConnectId getConnectId() {
    return publisher.connectId();
  }
}
