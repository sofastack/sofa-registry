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
package com.alipay.sofa.registry.common.model.metaserver;

import java.io.Serializable;

import com.alipay.sofa.registry.common.model.Node;

/**
 *
 * @author shangyu.wh
 * @version $Id: RenewNodesRequest.java, v 0.1 2018-03-30 19:51 shangyu.wh Exp $
 */
public class RenewNodesRequest<T extends Node> implements Serializable {

    private int     duration;

    private final T node;

    /**
     * constructor
     * @param node
     */
    public RenewNodesRequest(T node) {
        this.node = node;
    }

    /**
     * Getter method for property <tt>duration</tt>.
     *
     * @return property value of duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Setter method for property <tt>duration</tt>.
     *
     * @param duration  value to be assigned to property duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Getter method for property <tt>node</tt>.
     *
     * @return property value of node
     */
    public T getNode() {
        return node;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RenewNodesRequest{");
        sb.append("duration=").append(duration);
        sb.append(", node=").append(node);
        sb.append('}');
        return sb.toString();
    }
}