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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The type Net util.
 * @author shangyu.wh
 * @version $Id : NetUtil.java, v 0.1 2017-11-22 12:13 shangyu.wh Exp $
 */
public class NetUtil {

    private static final Logger         LOGGER                           = LoggerFactory
                                                                             .getLogger(NetUtil.class);

    /**
     * The constant LOCALHOST.
     */
    public static final String          LOCALHOST                        = "127.0.0.1";

    /**
     * The constant ANYHOST.
     */
    public static final String          ANYHOST                          = "0.0.0.0";

    /** symbol : */
    public static final char            COLON                            = ':';

    private static volatile InetAddress LOCAL_ADDRESS                    = null;

    private static final Pattern        IP_PATTERN                       = Pattern
                                                                             .compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public static final String          NETWORK_INTERFACE_BINDING        = "network_interface_binding";
    public static final String          NETWORK_INTERFACE_BINDING_VALUE  = System
                                                                             .getProperty(NETWORK_INTERFACE_BINDING);
    public static final String          NETWORK_INTERFACE_DENYLIST       = "network_interface_denylist";
    public static final List<String>    NETWORK_INTERFACE_DENYLIST_VALUE = System
                                                                             .getProperty(NETWORK_INTERFACE_DENYLIST) == null ? Collections
                                                                             .emptyList()
                                                                             : Arrays
                                                                                 .asList(System
                                                                                     .getProperty(
                                                                                         NETWORK_INTERFACE_DENYLIST)
                                                                                     .split(","));

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
            throw new RuntimeException("Unknown host {" + domain + "}");
        }
    }

    /**
     *
     * @param ip
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
            LOGGER
                .error("InetSocketAddress to Address String error!In put inetSocketAddress or InetSocketAddress.getAddress is null");
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
        return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN
            .matcher(name).matches());
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        boolean useNi;
                        if (NETWORK_INTERFACE_BINDING_VALUE != null
                            && !NETWORK_INTERFACE_BINDING_VALUE.isEmpty()) {
                            if (NETWORK_INTERFACE_BINDING_VALUE.equals(network.getDisplayName())
                                || NETWORK_INTERFACE_BINDING_VALUE.equals(network.getName())) {
                                useNi = true;
                            } else {
                                continue;
                            }
                        } else {
                            if (NETWORK_INTERFACE_DENYLIST_VALUE.contains(network.getDisplayName())
                                || NETWORK_INTERFACE_DENYLIST_VALUE.contains(network.getName())) {
                                continue;
                            }
                            useNi = true;
                        }

                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (useNi && isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    LOGGER.warn(
                                        "Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        LOGGER.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }
}