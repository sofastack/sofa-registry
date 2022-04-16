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
package com.alipay.sofa.registry.common.model.slot;

import com.alipay.sofa.registry.common.model.GenericResponse;

/**
 * @author yuzhi.lyz
 * @version v 0.1 2020-10-30 11:05 yuzhi.lyz Exp $
 */
public final class SlotAccessGenericResponse<T> extends GenericResponse<T> {
  private SlotAccess slotAccess;

  public SlotAccessGenericResponse(boolean success, String message, SlotAccess slotAccess, T data) {
    this.slotAccess = slotAccess;
    this.setData(data);
    this.setSuccess(success);
    this.setMessage(message);
  }

  /**
   * Getter method for property <tt>slotAccess</tt>.
   *
   * @return property value of slotAccess
   */
  public SlotAccess getSlotAccess() {
    return slotAccess;
  }

  public static <T> SlotAccessGenericResponse<T> successResponse(SlotAccess access, T data) {
    return new SlotAccessGenericResponse(true, null, access, data);
  }

  public static <T> SlotAccessGenericResponse<T> failedResponse(SlotAccess access) {
    return new SlotAccessGenericResponse(false, access.toString(), access, null);
  }

  public static <T> SlotAccessGenericResponse<T> failedResponse(SlotAccess access, String msg) {
    return new SlotAccessGenericResponse(false, msg, access, null);
  }

  public static <T> SlotAccessGenericResponse<T> failedResponse(String msg) {
    return new SlotAccessGenericResponse(false, msg, null, null);
  }
}
