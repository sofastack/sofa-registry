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
package com.alipay.sofa.registry.common.model.constants;

/**
 *
 * @author zhuoyu.sjw
 * @version $Id: ValueConstants.java, v 0.1 2018-03-28 23:07 zhuoyu.sjw Exp $$
 */
public class ValueConstants {

    /**
     * connectId: sourceAddress_targetAddress
     */
    public static final String   CONNECT_ID_SPLIT                      = "_";


    /**
     * The constant DEFAULT_GROUP.
     */
    public static final String   DEFAULT_GROUP                         = "DEFAULT_GROUP";

    /**
     * The constant DEFAULT_ZONE.
     */
    public static final String   DEFAULT_ZONE                          = "DEFAULT_ZONE";

    public static final String   DEFAULT_INSTANCE_ID                   = "DEFAULT_INSTANCE_ID";

    /**
     * The constant DEFAULT_DATA_CENTER.
     */
    public static final String   DEFAULT_DATA_CENTER                   = "DefaultDataCenter";

    public static final long     DEFAULT_NO_DATUM_VERSION              = 1L;

    private static final Integer SYSTEM_RAFT_PORT                      = Integer
                                                                           .getInteger("RAFT_SERVER_PORT");

    public static final int      RAFT_SERVER_PORT                      = SYSTEM_RAFT_PORT != null ? SYSTEM_RAFT_PORT
                                                                           : 9614;

    private static final String  SYSTEM_RAFT_GROUP                     = System
                                                                           .getProperty("RAFT_SERVER_GROUP");

    public static final String   RAFT_SERVER_GROUP                     = SYSTEM_RAFT_GROUP != null ? SYSTEM_RAFT_GROUP
                                                                           : "RegistryGroup";

    public static final String   STOP_PUSH_DATA_SWITCH_DATA_ID         = "session.stop.push.data.switch#@#9600#@#CONFIG";

    public static final String   BLACK_LIST_DATA_ID                    = "session.blacklist.data#@#9600#@#CONFIG";

    public static final String   ENABLE_DATA_RENEW_SNAPSHOT            = "session.enable.datum.renew.switch#@#9600#@#CONFIG";

    public static final String   ENABLE_DATA_DATUM_EXPIRE              = "data.enable.datum.expire.switch#@#9600#@#CONFIG";

    public static final String   LOGGER_NAME_RENEW                     = "RENEW-LOGGER";

    /**
     * switch key for dataId sensitive is disable or not
     */
    public static final String   DISABLE_DATA_ID_CASE_SENSITIVE_SWITCH = "disable.dataId.case.sensitive";
    /**
     * switch for dataId sensitive is disable or not, default value is false which means dataId is case sensitive
     */
    public static final Boolean  DISABLE_DATA_ID_CASE_SENSITIVE        = Boolean
                                                                           .valueOf(System
                                                                               .getProperty(DISABLE_DATA_ID_CASE_SENSITIVE_SWITCH));

}
