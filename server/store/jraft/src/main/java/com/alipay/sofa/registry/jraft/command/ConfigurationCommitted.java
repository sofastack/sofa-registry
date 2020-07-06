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

import com.alipay.sofa.jraft.conf.Configuration;

import java.io.Serializable;

/**
 * @author xiangxu
 * @version : ConfigurationCommitted.java, v 0.1 2020年07月02日 7:54 下午 xiangxu Exp $
 */
public class ConfigurationCommitted implements Serializable {
    public String peers;

    public ConfigurationCommitted(String peers) {
        this.peers = peers;
    }
}