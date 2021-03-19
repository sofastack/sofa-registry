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
package com.alipay.sofa.registry.remoting.jersey.jetty.server;

import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannelOverHttp;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;

/**
 * @author shangyu.wh
 * @version $Id: HttpConnectionOver.java, v 0.1 2018-09-26 16:46 shangyu.wh Exp $
 */
public class HttpConnectionCustom extends HttpConnection {

  public HttpConnectionCustom(
      HttpConfiguration config,
      Connector connector,
      EndPoint endPoint,
      HttpCompliance compliance,
      boolean recordComplianceViolations) {
    super(config, connector, endPoint, compliance, recordComplianceViolations);
  }

  @Override
  protected HttpChannelOverHttp newHttpChannel() {
    return new HttpChannelOverHttpCustom(
        this, getConnector(), getHttpConfiguration(), getEndPoint(), this);
  }
}
