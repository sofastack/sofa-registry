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
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.registry.common.model.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author chen.zhu
 *     <p>Jan 12, 2021
 */
public final class NodeUtils {
  private NodeUtils() {}

  public static <T extends Node> List<String> transferNodeToIpList(Collection<T> nodes) {
    List<String> result = new ArrayList<>(nodes.size());
    nodes.forEach(node -> result.add(node.getNodeUrl().getIpAddress()));
    return result;
  }
}
