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
package com.alipay.sofa.registry.common.model.store;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author shangyu.wh
 * @version $Id: URL.java, v 0.1 2017-11-20 21:21 shangyu.wh Exp $
 */
public final class URL implements Serializable {
    private static final Logger LOGGER    = LoggerFactory.getLogger(URL.class);

    /** symbol : */
    public static final char    COLON     = ':';

    public static final byte    HESSIAN_2 = 1;

    public static final byte    PROTOBUF  = 11;

    public static final byte    JSON      = 2;

    private ProtocolType        protocol;

    private String              host;

    private String              ipAddress;

    private int                 port;

    private String              path;

    private Byte                serializerIndex;

    private Map<String, String> parameters;

    private String              addressString;

    /**
     * ProtocolType Enum
     */
    public enum ProtocolType {
        TR, BOLT, HTTP
    }

    public URL() {
    }

    /**
     * constructor
     * @param protocol
     * @param ipAddress
     * @param port
     * @param host
     * @param path
     * @param serializerIndex
     * @param parameters
     */
    public URL(ProtocolType protocol, String ipAddress, int port, String host, String path,
               Byte serializerIndex, Map<String, String> parameters) {
        this.protocol = protocol;
        this.host = host;
        this.ipAddress = WordCache.getInstance().getWordCache(getIPAddressFromDomain(ipAddress));
        this.port = port;
        this.addressString = WordCache.getInstance().getWordCache(buildAddressString());
        this.path = path;
        this.serializerIndex = serializerIndex;
        this.parameters = parameters;
    }

    /**
     * constructor
     * @param ipAddress
     * @param port
     */
    public URL(String ipAddress, int port) {
        this(null, ipAddress, port, "", "", HESSIAN_2, null);
    }

    /**
     * constructor
     * @param address
     */
    public URL(InetSocketAddress address) {
        this(null, address.getAddress().getHostAddress(), address.getPort(), "", "", HESSIAN_2,
            null);
    }

    /**
     * constructor
     * @param address
     * @param serializerIndex
     */
    public URL(InetSocketAddress address, Byte serializerIndex) {
        this(null, address.getAddress().getHostAddress(), address.getPort(), "", "",
            serializerIndex, null);
    }

    /**
     * constructor
     * @param ipAddress
     */
    public URL(String ipAddress) {
        this(ipAddress, 0);
    }

    /**
     * url transfer to InetSocketAddress
     * @param url
     * @return
     */
    public static InetSocketAddress toInetSocketAddress(URL url) {
        return new InetSocketAddress(url.getIpAddress(), url.getPort());
    }

    /**
     * Getter method for property <tt>protocol</tt>.
     *
     * @return property value of protocol
     */
    public ProtocolType getProtocol() {
        return protocol;
    }

    /**
     * Getter method for property <tt>host</tt>.
     *
     * @return property value of host
     */
    public String getHost() {
        return host;
    }

    /**
     * Getter method for property <tt>ipAddress</tt>.
     *
     * @return property value of ipAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Getter method for property <tt>port</tt>.
     *
     * @return property value of port
     */
    public int getPort() {
        return port;
    }

    /**
     * Getter method for property <tt>path</tt>.
     *
     * @return property value of path
     */
    public String getPath() {
        return path;
    }

    /**
     * Getter method for property <tt>parameters</tt>.
     *
     * @return property value of parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Getter method for property <tt>addressString</tt>.
     *
     * @return property value of addressString
     */
    public String getAddressString() {
        return addressString;
    }

    /**
     * build address string
     * @return
     */
    public String buildAddressString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(ipAddress).append(COLON).append(port);
        return sb.toString();
    }

    /**
     * Getter method for property <tt>serializerIndex</tt>.
     *
     * @return property value of serializerIndex
     */
    public Byte getSerializerIndex() {
        return serializerIndex;
    }

    private String getIPAddressFromDomain(String domain) {
        try {
            InetAddress a = InetAddress.getByName(domain);
            return a.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.error("Can not resolve " + domain + " really ip.");
        }
        return domain;
    }

    /**
     * TODO Other protocol
     * @param url
     * @return
     */
    public static URL valueOf(String url) {

        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String ipAddress = "";
        String path = "";
        int port = 0;
        ProtocolType protocol = null;
        Map<String, String> parameters = null;

        int i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if (url.length() > 0) {
            ipAddress = url;
        }

        return new URL(protocol, ipAddress, port, "", path, HESSIAN_2, parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        URL url = (URL) o;
        return port == url.port && protocol == url.protocol && Objects.equals(host, url.host)
               && Objects.equals(ipAddress, url.ipAddress) && Objects.equals(path, url.path)
               && Objects.equals(parameters, url.parameters)
               && Objects.equals(addressString, url.addressString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, host, ipAddress, port, path, parameters, addressString);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("URL{");
        sb.append("address='").append(addressString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}