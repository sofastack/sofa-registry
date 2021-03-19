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

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;

/**
 * @author shangyu.wh
 * @version $Id: HttpConnectionOverFactory.java, v 0.1 2018-09-26 16:40 shangyu.wh Exp $
 */
public class HttpConnectionCustomFactory extends HttpConnectionFactory {

  public HttpConnectionCustomFactory() {
    super(new HttpConfiguration());
  }

  @Override
  public Connection newConnection(Connector connector, EndPoint endPoint) {
    HttpConnectionCustom conn =
        new HttpConnectionCustom(
            getHttpConfiguration(),
            connector,
            endPoint,
            getHttpCompliance(),
            isRecordHttpComplianceViolations());
    return configure(conn, connector, endPoint);
  }
}
