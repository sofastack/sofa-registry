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
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.util.CollectionUtils;

/**
 * @author xiaojian.xj
 * @version : NodeServerOperateInfo.java, v 0.1 2021年12月31日 14:52 xiaojian.xj Exp $
 */
public class NodeServerOperateInfo {

  private final Set<OperationInfo> metas = Sets.newHashSet();

  private final Set<OperationInfo> datas = Sets.newHashSet();

  private final Map<String /*cell*/, Set<OperationInfo>> sessions = Maps.newConcurrentMap();

  /**
   * get meta operating members
   *
   * @return Set
   */
  public synchronized Set<OperationInfo> getMetas() {
    return Sets.newHashSet(metas);
  }

  /**
   * get data operating members
   *
   * @return Set
   */
  public synchronized Set<OperationInfo> getDatas() {
    return Sets.newHashSet(datas);
  }

  public synchronized Map<String /*cell*/, Set<OperationInfo>> getSessions() {
    return Maps.newHashMap(sessions);
  }

  /**
   * get session operating members
   *
   * @return Set
   */
  public synchronized Set<OperationInfo> sessionNodes() {
    Set<OperationInfo> ret = Sets.newHashSet();
    sessions.forEach((cell, session) -> ret.addAll(session));
    return ret;
  }

  /**
   * Getter method for property <tt>metas</tt>.
   *
   * @return property value of metas
   */
  public synchronized int metasSize() {
    return metas.size();
  }

  /**
   * Getter method for property <tt>datas</tt>.
   *
   * @return property value of datas
   */
  public synchronized int datasSize() {
    return datas.size();
  }

  /**
   * @param cell cell
   * @return int
   */
  public synchronized int sessionSize(String cell) {
    Set<OperationInfo> operationInfos = sessions.get(cell);
    return CollectionUtils.isEmpty(operationInfos) ? 0 : operationInfos.size();
  }

  public synchronized int sessionSize() {

    return Optional.ofNullable(sessions.values()).orElse(Collections.emptySet()).stream()
        .mapToInt(Set::size)
        .sum();
  }

  /**
   * Getter method for property <tt>metaLastOperateTs</tt>.
   *
   * @return property value of metaLastOperateTs
   */
  public synchronized long metaLastOperateTs() {
    if (CollectionUtils.isEmpty(metas)) {
      return 0;
    }
    Long max = metas.stream().map(OperationInfo::getOperateTs).max(Long::compareTo).orElse(0L);
    return max.longValue();
  }

  /**
   * Getter method for property <tt>dataLastOperateTs</tt>.
   *
   * @return property value of dataLastOperateTs
   */
  public synchronized long dataLastOperateTs() {
    if (CollectionUtils.isEmpty(datas)) {
      return 0;
    }
    Long max = datas.stream().map(OperationInfo::getOperateTs).max(Long::compareTo).orElse(0L);
    return max.longValue();
  }

  public synchronized long sessionLastOperateTs() {
    long ret = 0;
    for (Set<OperationInfo> operationInfos :
        Optional.ofNullable(sessions.values()).orElse(Collections.emptySet())) {
      if (CollectionUtils.isEmpty(operationInfos)) {
        continue;
      }
      Long max =
          operationInfos.stream().map(OperationInfo::getOperateTs).max(Long::compareTo).orElse(0L);
      if (max.longValue() > ret) {
        ret = max.longValue();
      }
    }
    return ret;
  }

  /**
   * add meta operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean addMetas(String cell, String address) {
    return metas.add(new OperationInfo(NodeType.META, cell, address, System.currentTimeMillis()));
  }

  /**
   * remove meta operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean removeMetas(String cell, String address) {
    return metas.remove(
        new OperationInfo(NodeType.META, cell, address, System.currentTimeMillis()));
  }

  /**
   * add data operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean addDatas(String cell, String address) {
    return datas.add(new OperationInfo(NodeType.DATA, cell, address, System.currentTimeMillis()));
  }

  /**
   * remove data operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean removeDatas(String cell, String address) {
    return datas.remove(
        new OperationInfo(NodeType.DATA, cell, address, System.currentTimeMillis()));
  }

  /**
   * add session operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean addSessions(String cell, String address) {
    Set<OperationInfo> operationInfos = sessions.computeIfAbsent(cell, k -> Sets.newHashSet());
    return operationInfos.add(
        new OperationInfo(NodeType.SESSION, cell, address, System.currentTimeMillis()));
  }

  /**
   * remove session operation
   *
   * @param cell cell
   * @param address address
   * @return boolean
   */
  public synchronized boolean removeSessions(String cell, String address) {

    Set<OperationInfo> operationInfos = sessions.get(cell);
    if (CollectionUtils.isEmpty(operationInfos)) {
      return false;
    }
    return operationInfos.remove(
        new OperationInfo(NodeType.SESSION, cell, address, System.currentTimeMillis()));
  }

  @Override
  public String toString() {
    return "NodeServerOperateInfo{"
        + "metas="
        + metas
        + ", datas="
        + datas
        + ", sessions="
        + sessions
        + '}';
  }
}
