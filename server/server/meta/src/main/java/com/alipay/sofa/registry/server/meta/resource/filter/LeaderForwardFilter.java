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
package com.alipay.sofa.registry.server.meta.resource.filter;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.meta.MetaLeaderService;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/24
 */
@Provider
@LeaderForwardRestController
@Priority(Priorities.USER)
public class LeaderForwardFilter implements ContainerRequestFilter {

  private Logger LOGGER =
      LoggerFactory.getLogger(LeaderForwardFilter.class, "[LeaderForwardFilter]");

  @Autowired private MetaLeaderService metaLeaderService;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    if (metaLeaderService.amILeader()) {
      return;
    }

    String leaderAddr = metaLeaderService.getLeader();
    if (StringUtils.isBlank(leaderAddr)) {
      Response response =
          Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .header("reason", "no leader found")
              .build();
      requestContext.abortWith(response);
      return;
    }

    this.proxyRequestToMetaLeader(requestContext, leaderAddr);
  }

  /**
   * 当前 Meta 不是 Leader，将请求转发到 Meta Leader 中 这里没考虑直接使用 Http Client 是因为使用到这个 Filter 的基本都是配置下发链路
   * 属于旁路，不需要链接池、内存池等各种资源，因此就不想引入其他依赖了，只使用了 Java 原生的 方法，链接也是使用到的时候再主动去链
   */
  private void proxyRequestToMetaLeader(ContainerRequestContext requestContext, String leaderAddr) {
    HttpURLConnection connection = null;
    try {
      // 1. 获取请求信息
      String method = requestContext.getMethod();
      boolean hasEntity = requestContext.hasEntity();
      MultivaluedMap<String, String> headers = requestContext.getHeaders();
      UriInfo uriInfo = requestContext.getUriInfo();
      URI requestURI = uriInfo.getAbsolutePath();
      int requestPort = requestURI.getPort();
      String requestPath = requestURI.getRawPath();

      // 2. 拼接发送给 Meta Leader 的请求地址
      String newRequestURLStr =
          String.format("http://%s:%d%s", leaderAddr, requestPort, requestPath);
      URL newRequestURL = new URL(newRequestURLStr);

      // 3. 打开链接，这里因为协议写死是 HTTP 协议，所以拿到的必然是 HttpURLConnection
      connection = (HttpURLConnection) newRequestURL.openConnection();
      connection.setRequestMethod(method);
      if (hasEntity) {
        connection.setDoOutput(true);
      }
      connection.setDoInput(true);

      // 设置超时时间
      connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(1));
      connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(3));

      // 设置请求头
      for (MultivaluedMap.Entry<String, List<String>> entry : headers.entrySet()) {
        String headerKey = entry.getKey();
        List<String> headerValues = entry.getValue();
        if (CollectionUtils.isNotEmpty(headerValues)) {
          connection.addRequestProperty(headerKey, headerValues.get(0));
        }
      }

      connection.connect();

      // 4. 发送请求，直接做一次拷贝
      if (hasEntity) {
        try (OutputStream outputStream = connection.getOutputStream();
            InputStream inputStream = requestContext.getEntityStream()) {
          IOUtils.copy(inputStream, outputStream);
        }
      }

      // 5. 接收响应
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpStatus.OK_200) {
        LOGGER.error(
            "Proxy request to meta leader fail, response code: {}, message: {}",
            responseCode,
            connection.getResponseMessage());
        Response response =
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header(
                    "reason",
                    "proxy request to meta leader fail: " + connection.getResponseMessage())
                .build();
        requestContext.abortWith(response);
        return;
      }

      // 读取数据
      try (InputStream inputStream = connection.getInputStream();
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024); ) {
        IOUtils.copy(inputStream, outputStream);
        byte[] responseData = outputStream.toByteArray();
        Response response = Response.ok(responseData).build();
        requestContext.abortWith(response);
      }
    } catch (Throwable throwable) {
      LOGGER.error(
          "Proxy request to meta leader exception, meta leader address: {}", leaderAddr, throwable);
      Response response =
          Response.status(Response.Status.INTERNAL_SERVER_ERROR)
              .header("reason", "proxy request to meta leader exception")
              .build();
      requestContext.abortWith(response);
    } finally {
      if (null != connection) {
        try {
          connection.disconnect();
        } catch (Throwable throwable) {
          // 吃掉异常
          LOGGER.error(
              "Disconnect connection to meta leader fail, meta leader address: {}",
              leaderAddr,
              throwable);
        }
      }
    }
  }

  @VisibleForTesting
  public LeaderForwardFilter setMetaLeaderService(MetaLeaderService metaLeaderService) {
    this.metaLeaderService = metaLeaderService;
    return this;
  }
}
