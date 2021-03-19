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
package com.alipay.sofa.registry.server.meta.slot.util;

import static java.util.stream.Collectors.toList;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import java.util.Collections;
import java.util.List;

/**
 * @author xiaojian.xj
 * @version $Id: ListUtil.java, v 0.1 2021年02月02日 21:02 xiaojian.xj Exp $
 */
public class ListUtil {

  /**
   * 取差集
   *
   * @param list1
   * @param list2
   * @return
   */
  public static List<DataNode> reduce(List<DataNode> list1, List<DataNode> list2) {
    List<DataNode> reduce = list1.stream().filter(item -> !list2.contains(item)).collect(toList());

    return reduce;
  }

  /**
   * random pick
   *
   * @param list
   * @param count
   * @return
   */
  public static List<DataNode> randomPick(List<DataNode> list, int count) {
    if (list.size() <= count) {
      return list;
    }

    Collections.shuffle(list);
    return list.subList(0, count);
  }
}
