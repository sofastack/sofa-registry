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
package com.alipay.sofa.registry.jraft.command;

import java.io.Serializable;

/**
 * @author shangyu.wh
 * @version $Id: Response.java, v 0.1 2018-05-21 14:22 shangyu.wh Exp $
 */
public class ProcessResponse implements Serializable {

  private final Object entity;

  private final Boolean success;

  private final String redirect;

  /**
   * constructor
   *
   * @param entity entity
   * @param success success
   * @param redirect redirect
   */
  public ProcessResponse(Object entity, Boolean success, String redirect) {
    this.entity = entity;
    this.success = success;
    this.redirect = redirect;
  }

  /**
   * response ok
   *
   * @return
   */
  public static ResponseBuilder ok() {
    return setStatus(true);
  }

  /**
   * response ok
   *
   * @param entity entity
   * @return ResponseBuilder
   */
  public static ResponseBuilder ok(Object entity) {
    ResponseBuilder b = ok();
    b.entity(entity);
    return b;
  }

  /**
   * response fail
   *
   * @return ResponseBuilder
   */
  public static ResponseBuilder fail() {
    return setStatus(false);
  }

  /**
   * response fail
   *
   * @param errorMsg errorMsg
   * @return ResponseBuilder
   */
  public static ResponseBuilder fail(String errorMsg) {
    ResponseBuilder b = fail();
    b.entity(errorMsg);
    return b;
  }

  /**
   * response redirect
   *
   * @param leader leader
   * @return ResponseBuilder
   */
  public static ResponseBuilder redirect(String leader) {
    ResponseBuilder b = fail();
    b.entity("Not leader");
    b.redirect(leader);
    return b;
  }

  protected static ResponseBuilder setStatus(Boolean status) {
    ResponseBuilder b = new ResponseBuilder();
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
   * Getter method for property <tt>success</tt>.
   *
   * @return property value of success
   */
  public Boolean getSuccess() {
    return success;
  }

  /**
   * Getter method for property <tt>redirect</tt>.
   *
   * @return property value of redirect
   */
  public String getRedirect() {
    return redirect;
  }

  /** ResponseBuilder */
  public static class ResponseBuilder {
    private Object entity;

    private Boolean success;
    private String redirect;

    public ProcessResponse build() {
      final ProcessResponse r = new ProcessResponse(entity, success, redirect);
      reset();
      return r;
    }

    private void reset() {
      success = null;
      entity = null;
      redirect = null;
    }

    public ResponseBuilder status(Boolean status) {
      if (status == null) {
        throw new IllegalArgumentException("response status can not be null!");
      }
      this.success = status;
      return this;
    }

    public ResponseBuilder entity(Object entity) {
      this.entity = entity;
      return this;
    }

    public ResponseBuilder redirect(String redirect) {
      if (redirect == null) {
        throw new IllegalArgumentException("redirect leader can not be null!");
      }
      this.redirect = redirect;
      return this;
    }
  }
}
