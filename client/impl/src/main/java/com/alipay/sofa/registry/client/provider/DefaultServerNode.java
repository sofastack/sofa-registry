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
package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.remoting.ServerNode;
import java.util.Properties;

/**
 * The type Default server node.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultServerNode.java, v 0.1 2018-03-01 17:13 zhuoyu.sjw Exp $$
 */
public class DefaultServerNode implements ServerNode {

  private String url;

  private String host;

  private int port;

  private Properties properties;

  /**
   * Instantiates a new Default server node.
   *
   * @param url the url
   * @param host the host
   * @param port the port
   * @param properties the properties
   */
  public DefaultServerNode(String url, String host, int port, Properties properties) {
    this.url = url;
    this.host = host;
    this.port = port;
    this.properties = properties;
  }

  /**
   * Gets host.
   *
   * @return the host
   */
  @Override
  public String getHost() {
    return host;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  @Override
  public int getPort() {
    return port;
  }

  /**
   * Gets url.
   *
   * @return the url
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * Gets properties.
   *
   * @return the properties
   */
  @Override
  public Properties getProperties() {
    return properties;
  }

  /**
   * Setter method for property <tt>url</tt>.
   *
   * @param url value to be assigned to property url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Setter method for property <tt>host</tt>.
   *
   * @param host value to be assigned to property host
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Setter method for property <tt>port</tt>.
   *
   * @param port value to be assigned to property port
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Setter method for property <tt>properties</tt>.
   *
   * @param properties value to be assigned to property properties
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /** @see Object#equals(Object) */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultServerNode)) {
      return false;
    }

    DefaultServerNode that = (DefaultServerNode) o;

    if (port != that.port) {
      return false;
    }
    if (url != null ? !url.equals(that.url) : that.url != null) {
      return false;
    }
    if (host != null ? !host.equals(that.host) : that.host != null) {
      return false;
    }
    return properties != null ? properties.equals(that.properties) : that.properties == null;
  }

  /** @see Object#hashCode() */
  @Override
  public int hashCode() {
    int result = url != null ? url.hashCode() : 0;
    result = 31 * result + (host != null ? host.hashCode() : 0);
    result = 31 * result + port;
    result = 31 * result + (properties != null ? properties.hashCode() : 0);
    return result;
  }

  /** @see Object#toString() */
  @Override
  public String toString() {
    return "DefaultServerNode{"
        + "url='"
        + url
        + '\''
        + ", host='"
        + host
        + '\''
        + ", port="
        + port
        + ", properties="
        + properties
        + '}';
  }
}
