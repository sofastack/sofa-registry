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
package com.alipay.sofa.registry.server.session.converter.pb;

import com.alipay.sofa.registry.common.model.client.pb.RegisterResponsePb;
import com.alipay.sofa.registry.core.model.RegisterResponse;

/**
 * @author bystander
 * @version $Id: RegisterResponseConvertor.java, v 0.1 2018年03月21日 2:07 PM bystander Exp $
 */
public class RegisterResponseConvertor {

  public static RegisterResponse convert2Java(RegisterResponsePb registerResponsePb) {

    if (registerResponsePb == null) {
      return null;
    }

    RegisterResponse registerResponse = new RegisterResponse();

    registerResponse.setMessage(registerResponsePb.getMessage());
    registerResponse.setRefused(registerResponsePb.getRefused());
    registerResponse.setRegistId(registerResponsePb.getRegistId());
    registerResponse.setSuccess(registerResponsePb.getSuccess());
    registerResponse.setVersion(registerResponsePb.getVersion());

    return registerResponse;
  }

  public static RegisterResponsePb convert2Pb(RegisterResponse registerResponseJava) {

    if (registerResponseJava == null) {
      return null;
    }

    RegisterResponsePb.Builder builder = RegisterResponsePb.newBuilder();

    if (null != registerResponseJava.getMessage()) {
      builder.setMessage(registerResponseJava.getMessage());
    }
    if (null != registerResponseJava.getRegistId()) {
      builder.setRegistId(registerResponseJava.getRegistId());
    }
    builder.setVersion(registerResponseJava.getVersion());
    builder.setRefused(registerResponseJava.isRefused());
    builder.setSuccess(registerResponseJava.isSuccess());

    return builder.build();
  }
}
