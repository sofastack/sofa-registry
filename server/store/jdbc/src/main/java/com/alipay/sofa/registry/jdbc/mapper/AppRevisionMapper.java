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
package com.alipay.sofa.registry.jdbc.mapper;

import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionDomain;
import com.alipay.sofa.registry.jdbc.domain.AppRevisionQueryModel;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AppRevisionMapper.java, v 0.1 2021年01月18日 17:49 xiaojian.xj Exp $
 */
public interface AppRevisionMapper {

    /**
     * save record
     * @param appRevision
     * @return effect record count
     */
    public int insert(AppRevisionDomain appRevision);

    /**
     * query revision
     * @param dataCenter
     * @param revision
     * @return
     */
    public AppRevisionDomain queryRevision(@Param("dataCenter") String dataCenter,
                                           @Param("revision") String revision);

    /**
     * batch query
     * @param querys
     * @return
     */
    public List<AppRevisionDomain> batchQuery(List<AppRevisionQueryModel> querys);

    /**
     * check if revision exist
     * @param query
     * @return revision
     */
    AppRevisionDomain checkExist(AppRevisionQueryModel query);

    /**
     *
     * @param heartbeats
     */
    void batchHeartbeat(List<AppRevision> heartbeats);

    /**
     * query app_revision silence beyond silenceHour
     * @param dataCenter
     * @param silenceHour
     * @return
     */
    public List<AppRevisionDomain> queryGcRevision(@Param("dataCenter") String dataCenter,
                                                   @Param("silenceHour") int silenceHour,
                                                   @Param("limitCount") int limitCount);

    /**
     * delete
     * @param dataCenter
     * @param revision
     */
    public void deleteAppRevision(@Param("dataCenter") String dataCenter,
                                  @Param("revision") String revision);
}