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

/**
 * @author shangyu.wh
 * @version $Id: Watcher.java, v 0.1 2018-04-17 18:22 shangyu.wh Exp $
 */
public class Watcher extends BaseInfo {

  @Override
  public DataType getDataType() {
    return DataType.WATCHER;
  }

  /**
   * change watcher word cache
   *
   * @param watcher
   * @return
   */
  public static Watcher internWatcher(Watcher watcher) {
    watcher.setRegisterId(watcher.getRegisterId());
    watcher.setDataInfoId(watcher.getDataInfoId());
    watcher.setInstanceId(watcher.getInstanceId());
    watcher.setGroup(watcher.getGroup());
    watcher.setDataId(watcher.getDataId());
    watcher.setClientId(watcher.getClientId());
    watcher.setCell(watcher.getCell());
    watcher.setProcessId(watcher.getProcessId());
    watcher.setAppName(watcher.getAppName());

    return watcher;
  }
}
