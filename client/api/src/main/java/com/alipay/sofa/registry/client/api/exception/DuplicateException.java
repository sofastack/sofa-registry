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
package com.alipay.sofa.registry.client.api.exception;

/**
 * The type Duplicate exception.
 *
 * @author zhuoyu.sjw
 * @version $Id : DuplicateException.java, v 0.1 2017-11-30 19:46 zhuoyu.sjw Exp $$
 */
public class DuplicateException extends IllegalArgumentException {

  /** UID */
  private static final long serialVersionUID = 167969795120169890L;

  /** Instantiates a new Duplicate exception. */
  public DuplicateException() {}

  /**
   * Instantiates a new Duplicate exception.
   *
   * @param s the s
   */
  public DuplicateException(String s) {
    super(s);
  }

  /**
   * Instantiates a new Duplicate exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public DuplicateException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Instantiates a new Duplicate exception.
   *
   * @param cause the cause
   */
  public DuplicateException(Throwable cause) {
    super(cause);
  }
}
