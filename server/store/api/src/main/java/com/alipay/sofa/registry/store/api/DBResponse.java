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
package com.alipay.sofa.registry.store.api;

import java.io.Serializable;

/**
 * DBResponse
 *
 * @author shangyu.wh
 * @version $Id: DBResponse.java, v 0.1 2018-04-18 16:35 shangyu.wh Exp $
 */
public class DBResponse implements Serializable {

  private final Object entity;

  private final OperationStatus operationStatus;

  /**
   * @param entity
   * @param operationStatus
   */
  public DBResponse(Object entity, OperationStatus operationStatus) {
    this.entity = entity;
    this.operationStatus = operationStatus;
  }

  /**
   * generate response ok
   *
   * @return
   */
  public static DBResponseBuilder ok() {
    return setStatus(OperationStatus.SUCCESS);
  }

  /**
   * generate response ok
   *
   * @param entity
   * @return
   */
  public static DBResponseBuilder ok(Object entity) {
    DBResponseBuilder b = ok();
    b.entity(entity);
    return b;
  }

  /**
   * set operationStatus to NOTFOUND
   *
   * @return
   */
  public static DBResponseBuilder notfound() {
    return setStatus(OperationStatus.NOTFOUND);
  }

  /**
   * set operationStatus
   *
   * @param status
   * @return
   */
  protected static DBResponseBuilder setStatus(OperationStatus status) {
    DBResponseBuilder b = DBResponseBuilder.getInstance();
    b.status(status);
    return b;
  }

  /**
   * Getter method for property <tt>entity</tt>.
   *
   * @return property value of entity
   */
  public Object getEntity() {
    return entity;
  }

  /**
   * Getter method for property <tt>operationStatus</tt>.
   *
   * @return property value of operationStatus
   */
  public OperationStatus getOperationStatus() {
    return operationStatus;
  }

  /** DBResponseBuilder */
  public static class DBResponseBuilder {
    private static final DBResponseBuilder instance = new DBResponseBuilder();

    private DBResponseBuilder() {}

    /**
     * get DBResponseBuilder instance
     *
     * @return
     */
    public static DBResponseBuilder getInstance() {
      return instance;
    }

    private Object entity;

    private OperationStatus operationStatus;

    /**
     * build func
     *
     * @return
     */
    public DBResponse build() {
      final DBResponse r = new DBResponse(entity, operationStatus);
      reset();
      return r;
    }

    private void reset() {
      operationStatus = null;
      entity = null;
    }

    /**
     * set operationStatus status
     *
     * @param status
     * @return
     */
    public DBResponseBuilder status(OperationStatus status) {
      if (status == null) {
        throw new IllegalArgumentException();
      }
      this.operationStatus = status;
      return this;
    }

    /**
     * set entity
     *
     * @param entity
     * @return
     */
    public DBResponseBuilder entity(Object entity) {
      this.entity = entity;
      return this;
    }
  }
}
