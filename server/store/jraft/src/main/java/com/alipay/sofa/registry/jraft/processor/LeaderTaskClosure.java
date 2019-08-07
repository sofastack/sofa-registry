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
package com.alipay.sofa.registry.jraft.processor;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;

/**
 *
 * @author shangyu.wh
 * @version $Id: LeaderTaskClosure.java, v 0.1 2018-05-21 12:18 shangyu.wh Exp $
 */
public class LeaderTaskClosure implements Closure {

    private ProcessRequest request;
    private Closure        done;
    private Object         response;

    @Override
    public void run(Status status) {
        if (this.done != null) {
            done.run(status);
        }
    }

    /**
     * Getter method for property <tt>request</tt>.
     *
     * @return property value of request
     */
    public ProcessRequest getRequest() {
        return request;
    }

    /**
     * Setter method for property <tt>request</tt>.
     *
     * @param request  value to be assigned to property request
     */
    public void setRequest(ProcessRequest request) {
        this.request = request;
    }

    /**
     * Setter method for property <tt>done</tt>.
     *
     * @param done  value to be assigned to property done
     */
    public void setDone(Closure done) {
        this.done = done;
    }

    /**
     * Getter method for property <tt>response</tt>.
     *
     * @return property value of response
     */
    public Object getResponse() {
        return response;
    }

    /**
     * Setter method for property <tt>response</tt>.
     *
     * @param response  value to be assigned to property response
     */
    public void setResponse(Object response) {
        this.response = response;
    }
}