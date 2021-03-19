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
package com.alipay.sofa.registry.client.util;

import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The type Http client utils.
 *
 * @author zhuoyu.sjw
 * @version $Id : HttpClientUtils.java, v 0.1 2018-03-22 17:38 zhuoyu.sjw Exp $$
 */
public class HttpClientUtils {
  private static final char AND = '&';

  /**
   * Get string.
   *
   * @param url the url
   * @param params the params
   * @param config the config
   * @return the string
   * @throws Exception the exception
   */
  public static String get(String url, Map<String, String> params, RegistryClientConfig config)
      throws Exception {
    HttpURLConnection httpURLConnection = create(getFullPath(url, params), config);
    httpURLConnection.setRequestMethod("GET");

    BufferedReader reader = null;
    try {
      StringBuilder stringBuffer = new StringBuilder();

      int responseCode = httpURLConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        String charset = httpURLConnection.getContentEncoding();
        if (charset != null) {
          reader =
              new BufferedReader(
                  new InputStreamReader(httpURLConnection.getInputStream(), charset));
        } else {
          reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        }

        String strCurrentLine;
        while ((strCurrentLine = reader.readLine()) != null) {
          stringBuffer.append(strCurrentLine).append("\n");
        }
        if (stringBuffer.length() > 0 && stringBuffer.charAt(stringBuffer.length() - 1) == '\n') {
          stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }

        return stringBuffer.toString();
      }
    } finally {
      httpURLConnection.disconnect();
      if (null != reader) {
        reader.close();
      }
    }
    return null;
  }

  /**
   * Create http url connection.
   *
   * @param httpUrl the http url
   * @param config the config
   * @return the http url connection
   * @throws Exception the exception
   */
  private static HttpURLConnection create(String httpUrl, RegistryClientConfig config)
      throws Exception {
    URL url = new URL(httpUrl);
    URLConnection urlConnection = url.openConnection();
    HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
    httpConnection.setDoOutput(true);
    httpConnection.setDoInput(true);
    httpConnection.setUseCaches(false);
    httpConnection.setRequestProperty("Content-type", "text/plain");
    httpConnection.setConnectTimeout(config.getConnectTimeout());
    httpConnection.setReadTimeout(config.getSocketTimeout());
    return httpConnection;
  }

  private static String getFullPath(String url, Map<String, String> params) {
    StringBuilder sb = new StringBuilder(url);
    if (params != null) {
      sb.append("?");
      for (Entry<String, String> param : params.entrySet()) {
        sb.append(param.getKey());
        sb.append("=");
        sb.append(param.getValue());
        sb.append("&");
      }
      if (sb.charAt(sb.length() - 1) == AND) {
        sb.deleteCharAt(sb.length() - 1);
      }
    }
    return sb.toString();
  }
}
