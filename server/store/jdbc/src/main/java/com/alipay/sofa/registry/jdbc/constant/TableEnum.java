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
package com.alipay.sofa.registry.jdbc.constant;

/**
 * @author xiaojian.xj
 * @version : TableEnum.java, v 0.1 2021年09月27日 22:48 xiaojian.xj Exp $
 */
public enum TableEnum {
  PROVIDE_DATA("provide_data"),
  APP_REVISION("app_revision"),
  INTERFACE_APP_INDEX("interface_app_index"),
  DISTRIBUTE_LOCK("distribute_lock"),
  CLIENT_MANAGER_ADDRESS("client_manager_address"),
  MULTI_CLUSTER_SYNC_INFO("multi_cluster_sync_info"),
  ;

  private String tableName;

  TableEnum(String tableName) {
    this.tableName = tableName;
  }

  /**
   * Getter method for property <tt>tableName</tt>.
   *
   * @return property value of tableName
   */
  public String getTableName() {
    return tableName;
  }
}
