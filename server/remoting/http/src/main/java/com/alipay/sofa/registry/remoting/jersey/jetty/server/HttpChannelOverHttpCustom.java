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

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannelOverHttp;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.HttpTransport;

/**
 * @author shangyu.wh
 * @version $Id: HttpChannelOverHttpHack.java, v 0.1 2018-09-26 17:00 shangyu.wh Exp $
 */
public class HttpChannelOverHttpCustom extends HttpChannelOverHttp {
  private static final char QUESTION_MARK = '?';

  public HttpChannelOverHttpCustom(
      HttpConnection httpConnection,
      Connector connector,
      HttpConfiguration config,
      EndPoint endPoint,
      HttpTransport transport) {
    super(httpConnection, connector, config, endPoint, transport);
  }

  @Override
  public boolean startRequest(String method, String uri, HttpVersion version) {

    if (uri != null && !uri.isEmpty() && uri.charAt(0) == QUESTION_MARK) {
      /* HTTP/1.1 spec says in 5.1.2. about Request-URI:
       * "Note that the absolute path cannot be empty; if
       * none is present in the original URI, it MUST be
       * given as "/" (the server root)."  So if the file
       * name here has only a query string, the path is
       * empty and we also have to add a "/".
       */
      uri = "/" + uri;
    }
    return super.startRequest(method, uri, version);
  }
}
