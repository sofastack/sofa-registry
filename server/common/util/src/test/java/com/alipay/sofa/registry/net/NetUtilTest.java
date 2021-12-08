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

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.alipay.sofa.registry.TestUtils;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import org.junit.*;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NetUtil.class, NetworkInterface.class})
@PowerMockIgnore({"javax.management.*"})
public class NetUtilTest {
  private static InetAddress localAddr;
  private static InetAddress eth0Addr;
  private static InetAddress addr1;
  private static InetAddress addr2;

  private static NetworkInterface localNI;
  private static NetworkInterface eth0NI;
  private static NetworkInterface addr1NI;
  private static NetworkInterface addr2NI;

  private static Field BINDING_FIELD;
  private static Field DENY_FIELD;

  @BeforeClass
  public static void beforeClass() throws Exception {
    BINDING_FIELD = NetUtil.class.getDeclaredField("NETWORK_INTERFACE_BINDING_VALUE");
    BINDING_FIELD.setAccessible(true);
    DENY_FIELD = NetUtil.class.getDeclaredField("NETWORK_INTERFACE_DENYLIST_VALUE");
    DENY_FIELD.setAccessible(true);
  }

  private void mockNetInterfaces() {
    localAddr = mock(InetAddress.class);
    when(localAddr.isLoopbackAddress()).thenReturn(true);
    when(localAddr.getHostAddress()).thenReturn("127.0.0.1");

    eth0Addr = mock(InetAddress.class);
    when(eth0Addr.isLoopbackAddress()).thenReturn(false);
    when(eth0Addr.getHostAddress()).thenReturn("192.168.0.133");

    addr1 = mock(InetAddress.class);
    when(addr1.isLoopbackAddress()).thenReturn(false);
    when(addr1.getHostAddress()).thenReturn("172.19.0.2");

    addr2 = mock(InetAddress.class);
    when(addr2.isLoopbackAddress()).thenReturn(false);
    when(addr2.getHostAddress()).thenReturn("172.20.0.1");

    localNI = mock(NetworkInterface.class);
    when(localNI.getName()).thenReturn("lo");
    when(localNI.getDisplayName()).thenReturn("lo");
    when(localNI.getInetAddresses())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localAddr)));

    eth0NI = mock(NetworkInterface.class);
    when(eth0NI.getName()).thenReturn("eth0");
    when(eth0NI.getDisplayName()).thenReturn("eth0");
    when(eth0NI.getInetAddresses())
        .thenReturn(Collections.enumeration(Lists.newArrayList(eth0Addr)));

    addr1NI = mock(NetworkInterface.class);
    when(addr1NI.getName()).thenReturn("addr1");
    when(addr1NI.getDisplayName()).thenReturn("addr1");
    when(addr1NI.getInetAddresses()).thenReturn(Collections.enumeration(Lists.newArrayList(addr1)));

    addr2NI = mock(NetworkInterface.class);
    when(addr2NI.getName()).thenReturn("addr2");
    when(addr2NI.getDisplayName()).thenReturn("addr2");
    when(addr2NI.getInetAddresses()).thenReturn(Collections.enumeration(Lists.newArrayList(addr2)));
  }

  @Before
  public void before() throws IllegalAccessException {
    BINDING_FIELD.set(null, "");
    DENY_FIELD.set(null, Lists.newArrayList());
    PowerMockito.mockStatic(NetworkInterface.class);
    mockNetInterfaces();
  }

  @Test
  public void testNoInterfaces() throws Exception {
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList()));
    TestUtils.assertException(RuntimeException.class, NetUtil::getLocalAddress0);
  }

  @Test
  public void testBindValue() throws Exception {
    BINDING_FIELD.set(null, "addr1");
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localNI, eth0NI, addr1NI, addr2NI)));
    Assert.assertEquals(addr1.getHostAddress(), NetUtil.getLocalAddress0().getHostAddress());
  }

  @Test
  public void testPreference() throws SocketException {
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localNI, eth0NI, addr1NI, addr2NI)));
    Assert.assertEquals(eth0Addr.getHostAddress(), NetUtil.getLocalAddress0().getHostAddress());
  }

  @Test
  public void testIgnoreLocal() throws SocketException {
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localNI)));

    TestUtils.assertException(RuntimeException.class, NetUtil::getLocalAddress0);
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localNI, addr1NI)));
    Assert.assertEquals(addr1.getHostAddress(), NetUtil.getLocalAddress0().getHostAddress());
  }

  @Test
  public void testDeny() throws Exception {
    DENY_FIELD.set(null, Lists.newArrayList("eth0", "addr1"));
    when(NetworkInterface.getNetworkInterfaces())
        .thenReturn(Collections.enumeration(Lists.newArrayList(localNI, eth0NI, addr1NI, addr2NI)));
    Assert.assertEquals(addr2.getHostAddress(), NetUtil.getLocalAddress0().getHostAddress());
  }
}
