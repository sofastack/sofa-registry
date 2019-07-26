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
package com.alipay.sofa.registry.remoting.exchange;

import com.alipay.sofa.registry.remoting.exchange.message.Request;

/**
 *
 * @author shangyu.wh
 * @version $Id: RequestException.java, v 0.1 2018-01-15 18:16 shangyu.wh Exp $
 */
public class RequestException extends Exception {

    private Request request;

    /**
     * constructor
     * @param message
     * @param request
     */
    public RequestException(String message, Request request) {
        super(message);
        this.request = request;
    }

    /**
     * constructor
     * @param message
     * @param request
     * @param cause
     */
    public RequestException(String message, Request request, Throwable cause) {
        super(message, cause);
        this.request = request;
    }

    /**
     * constructor
     * @param message
     */
    public RequestException(String message) {
        super(message);
    }

    /**
     * constructor
     * @param message
     * @param cause
     */
    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * constructor
     * @param cause
     */
    public RequestException(Throwable cause) {
        super(cause);
    }

    /**
     * get requestInfo from Request
     * @return
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (request != null) {
            sb.append("request url: ").append(request.getRequestUrl()).append(", body: ")
                .append(request.getRequestBody()).append(", ");
        }
        sb.append("message: ").append(super.getMessage());
        return sb.toString();
    }
}