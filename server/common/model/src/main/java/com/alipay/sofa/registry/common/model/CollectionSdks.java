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
package com.alipay.sofa.registry.common.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * @author xiaojian.xj
 * @version $Id: CollectionSdks.java, v 0.1 2021年06月02日 14:31 xiaojian.xj Exp $
 */
public class CollectionSdks {

  public static List<String> toIpList(String ips) {
    String[] ipArray = StringUtils.split(ips.trim(), ';');
    List<String> ret = Lists.newArrayListWithCapacity(ipArray.length);
    for (String ip : ipArray) {
      ret.add(ip.trim());
    }
    return ret;
  }

  public static Set<String> toIpSet(String ips) {
    return Sets.newHashSet(toIpList(ips));
  }
}
