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
package com.alipay.sofa.registry.net;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.StringFormatter;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.collect.Lists;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * The type Net util.
 *
 * @author shangyu.wh
 * @version $Id : NetUtil.java, v 0.1 2017-11-22 12:13 shangyu.wh Exp $
 */
public class NetUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

  /** The constant LOCALHOST. */
  public static final String LOCALHOST = "127.0.0.1";

  /** The constant ANYHOST. */
  public static final String ANYHOST = "0.0.0.0";

  /** symbol : */
  public static final char COLON = ':';

  private static volatile InetAddress LOCAL_ADDRESS = null;

  private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

  public static final List<String> NETWORK_INTERFACE_PREFERENCE =
      Collections.unmodifiableList(Lists.newArrayList("eth0", "en0"));
  public static final String NETWORK_INTERFACE_BINDING = "network_interface_binding";
  public static final String NETWORK_INTERFACE_BINDING_VALUE =
      SystemUtils.getSystem(NETWORK_INTERFACE_BINDING);
  public static final String NETWORK_INTERFACE_DENYLIST = "network_interface_denylist";
  public static final List<String> NETWORK_INTERFACE_DENYLIST_VALUE =
      SystemUtils.getSystem(NETWORK_INTERFACE_DENYLIST) == null
          ? Collections.emptyList()
          : Arrays.asList(
              StringUtils.trim(System.getProperty(NETWORK_INTERFACE_DENYLIST)).split(","));

  /**
   * Gen host string.
   *
   * @param ip the ip
   * @param port the port
   * @return the string
   */
  public static String genHost(String ip, int port) {
    return ip + COLON + port;
  }

  /**
   * Gets local socket address.
   *
   * @return the local socket address
   */
  public static InetSocketAddress getLocalSocketAddress() {
    InetAddress address = getLocalAddress();
    String addressStr = address == null ? LOCALHOST : address.getHostAddress();
    return new InetSocketAddress(addressStr, 0);
  }

  /**
   * Gets ip address from domain.
   *
   * @param domain the domain
   * @return the ip address from domain
   */
  public static String getIPAddressFromDomain(String domain) {
    try {
      InetAddress a = InetAddress.getByName(domain);
      if (LOCALHOST.equalsIgnoreCase(a.getHostAddress())) {
        a = getLocalAddress();
      }
      return a.getHostAddress();
    } catch (UnknownHostException e) {
      throw new RuntimeException("Unknown host {" + domain + "}", e);
    }
  }

  /**
   * @param ip ip
   * @return String
   */
  public static String getDomainFromIP(String ip) {
    try {
      InetAddress a = InetAddress.getByName(ip);
      return a.getCanonicalHostName();
    } catch (UnknownHostException e) {
      LOGGER.error("[NetWorkUtils] Can not resolve ip " + ip + ". error is .", e);
    }
    return null;
  }

  /**
   * Gets local address.
   *
   * @return loccal IP all network card
   */
  public static InetAddress getLocalAddress() {
    if (LOCAL_ADDRESS != null) {
      return LOCAL_ADDRESS;
    }
    InetAddress localAddress = getLocalAddress0();
    LOCAL_ADDRESS = localAddress;
    return localAddress;
  }

  /**
   * To address string string.
   *
   * @param address the address
   * @return the string
   */
  public static String toAddressString(InetSocketAddress address) {
    if (address == null || address.getAddress() == null) {
      LOGGER.error(
          "InetSocketAddress to Address String error!In put inetSocketAddress or InetSocketAddress.getAddress is null");
      throw new RuntimeException(
          "InetSocketAddress to Address String error!In put inetSocketAddress or InetSocketAddress.getAddress is null!");
    }
    return address.getAddress().getHostAddress() + COLON + address.getPort();
  }

  private static boolean isValidAddress(InetAddress address) {
    if (address == null || address.isLoopbackAddress()) {
      return false;
    }
    String name = address.getHostAddress();
    return (name != null
        && !ANYHOST.equals(name)
        && !LOCALHOST.equals(name)
        && IP_PATTERN.matcher(name).matches());
  }

  static InetAddress getLocalAddress0() {
    try {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      if (CollectionUtils.isEmpty(interfaces)) {
        throw new RuntimeException("no network interfaces");
      }
      if (StringUtils.isNotBlank(NETWORK_INTERFACE_BINDING_VALUE)) {
        InetAddress localAddress =
            selectLocalAddress(
                interfaces, Collections.singletonList(NETWORK_INTERFACE_BINDING_VALUE));
        if (localAddress != null) {
          return localAddress;
        }
        throw new RuntimeException(
            StringFormatter.format(
                "cannot found network interface {}", NETWORK_INTERFACE_BINDING_VALUE));
      }
      if (!CollectionUtils.isEmpty(NETWORK_INTERFACE_DENYLIST_VALUE)) {
        interfaces =
            interfaces.stream()
                .filter(
                    i ->
                        !NETWORK_INTERFACE_DENYLIST_VALUE.contains(i.getName())
                            && !NETWORK_INTERFACE_DENYLIST_VALUE.contains(i.getDisplayName()))
                .collect(Collectors.toList());
      }
      if (!CollectionUtils.isEmpty(NETWORK_INTERFACE_PREFERENCE)) {
        InetAddress localAddress = selectLocalAddress(interfaces, NETWORK_INTERFACE_PREFERENCE);
        if (localAddress != null) {
          return localAddress;
        }
      }
      for (NetworkInterface ni : interfaces) {
        List<InetAddress> addresses = Collections.list(ni.getInetAddresses());
        for (InetAddress address : addresses) {
          if (isValidAddress(address)) {
            return address;
          }
        }
      }
      throw new RuntimeException("no valid network address");
    } catch (Throwable e) {
      LOGGER.error("Failed to retriving ip address: ", e);
      throw new RuntimeException(
          StringFormatter.format("Failed to retriving ip address: {}", e.getMessage()));
    }
  }

  static InetAddress selectLocalAddress(List<NetworkInterface> nis, List<String> bindings) {
    for (NetworkInterface ni : nis) {
      if (!bindings.contains(ni.getDisplayName()) && !bindings.contains(ni.getName())) {
        continue;
      }
      List<InetAddress> addresses = Collections.list(ni.getInetAddresses());
      for (InetAddress address : addresses) {
        if (isValidAddress(address)) {
          return address;
        }
      }
    }
    return null;
  }
}
