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
package com.alipay.sofa.registry.core.model;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AssembleType.java, v 0.1 2020年10月27日 01:51 xiaojian.xj Exp $
 */
public enum AssembleType {

    /** sub app: only sub data where dataId = app  */
    sub_app,

    /** sub interface: only sub data where dataId = interface  */
    sub_interface,

    /** sub app and interface: sub data from app and interface  */
    sub_app_and_interface, ;

    public static boolean contains(String name) {
        for (AssembleType subDataType : values()) {
            if (subDataType.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
}