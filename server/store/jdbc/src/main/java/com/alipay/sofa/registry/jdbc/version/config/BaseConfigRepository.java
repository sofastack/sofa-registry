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
package com.alipay.sofa.registry.jdbc.version.config;

import com.alipay.sofa.registry.log.Logger;

/**
 * @author xiaojian.xj
 * @version : ConfigRepository.java, v 0.1 2022年04月15日 14:41 xiaojian.xj Exp $
 */
public abstract class BaseConfigRepository<T extends ConfigEntry> {

  private final String name;

  private final Logger logger;

  public BaseConfigRepository(String name, Logger logger) {
    this.name = name;
    this.logger = logger;
  }

  public boolean put(T entry) {
    if (entry == null) {
      logger.error("name: {} update config entry is null.", name);
      return false;
    }

    try {
      T exist = queryExistVersion(entry);
      if (exist == null) {
        // it will throw duplicate key exception when parallel invocation
        insert(entry);
        return true;
      }
      return put(entry, exist.getDataVersion());
    } catch (Throwable t) {
      logger.error("name: {} update config entry:{} error.", name, entry, t);
      return false;
    }
  }

  public boolean put(T entry, long expectVersion) {
    if (entry == null) {
      logger.error("name: {} update config entry is null.", name);
      return false;
    }
    try {
      if (entry.getDataVersion() <= expectVersion) {
        logger.error(
            "update config entry fail, update.version:{} <= expectVersion:{}",
            entry.getDataVersion(),
            expectVersion);
        return false;
      }

      if (expectVersion == 0) {
        // it will throw duplicate key exception when parallel invocation
        insert(entry);
        logger.info("insert entry:{} success.", entry);
        return true;
      }

      int affect = updateWithExpectVersion(entry, expectVersion);
      if (affect == 0) {
        logger.error(
            "update config entry fail, affect=0, expectVersion={}, entry:{}", expectVersion, entry);
        return false;
      }
      logger.info("update entry:{}, expectVersion:{} success.", entry, expectVersion);
      return true;
    } catch (Throwable t) {
      logger.error("name: {} update config entry:{} error.", name, entry, t);
      return false;
    }
  }

  protected abstract T queryExistVersion(T entry);

  protected abstract long insert(T entry);

  protected abstract int updateWithExpectVersion(T entry, long exist);
}
