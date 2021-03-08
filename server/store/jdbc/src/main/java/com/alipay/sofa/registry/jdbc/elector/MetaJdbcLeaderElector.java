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
package com.alipay.sofa.registry.jdbc.elector;

import com.alipay.sofa.registry.jdbc.config.MetaElectorConfig;
import com.alipay.sofa.registry.jdbc.domain.DistributeLockDomain;
import com.alipay.sofa.registry.jdbc.domain.FollowCompeteLockDomain;
import com.alipay.sofa.registry.jdbc.mapper.DistributeLockMapper;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 * @author xiaojian.xj
 * @version $Id: MetaJdbcLeaderElector.java, v 0.1 2021年03月12日 10:18 xiaojian.xj Exp $
 */
public class MetaJdbcLeaderElector extends AbstractLeaderElector {

    private static final Logger LOG = LoggerFactory.getLogger("META-ELECTOR",
            "[MetaJdbcLeaderElector]");


    private static final String lockName = "META-MASTER";

    @Autowired
    private DistributeLockMapper distributeLockMapper;

    @Autowired
    private MetaElectorConfig metaElectorConfig;

    private Map<ElectorRole, ElectorRoleService> electorRoleMap = Maps.newConcurrentMap();


    /**
     * start elect, return current leader
     * @return
     */
    @Override
    protected LeaderInfo doElect() {
        DistributeLockDomain lock = distributeLockMapper.queryDistLock(metaElectorConfig.getDataCenter(), lockName);

        /**
         * compete and return leader
         */
        if (lock == null) {
            return competeLeader(metaElectorConfig.getDataCenter());
        }

        ElectorRole role = amILeader(lock.getOwner()) ? ElectorRole.LEADER : ElectorRole.FOLLOWER;
        lock = electorRoleMap.get(role).onWorking(lock, myself());
        return new LeaderInfo(lock.getGmtModified().getTime(), lock.getOwner(), lock.getGmtModified(), lock.getDuration());
    }

    /**
     * compete and return leader
     * @param dataCenter
     * @return
     */
    private LeaderInfo competeLeader(String dataCenter) {
        DistributeLockDomain lock = new DistributeLockDomain(dataCenter, lockName, myself(),  metaElectorConfig.getLockExpireDuration());
        try {
            // throw exception if insert fail
            distributeLockMapper.competeLockOnInsert(lock);
            // compete finish.
            lock = distributeLockMapper.queryDistLock(dataCenter, lockName);

            if (LOG.isInfoEnabled()) {
                LOG.info("meta: {} compete success, become leader.", myself());
            }
        } catch (Throwable t) {
            // compete leader error, query current leader
            lock = distributeLockMapper.queryDistLock(dataCenter, lockName);
            if (LOG.isInfoEnabled()) {
                LOG.info("meta: {} compete error, leader is: {}.", myself(), lock.getOwner());
            }
        }
        return new LeaderInfo(lock.getGmtModified().getTime(), lock.getOwner(), lock.getGmtModified(), lock.getDuration());
    }

    /**
     * query current leader
     * @return
     */
    @Override
    protected LeaderInfo doQuery() {
        DistributeLockDomain lock = distributeLockMapper.queryDistLock(metaElectorConfig.getDataCenter(), lockName);
        if (lock == null) {
            return LeaderInfo.hasNoLeader;
        }

        return new LeaderInfo(lock.getGmtModified().getTime(), lock.getOwner(), lock.getGmtModified(), lock.getDuration());
    }

    public interface ElectorRoleService {
        DistributeLockDomain onWorking(DistributeLockDomain lock, String myself);

        ElectorRole support();
    }

    public class LeaderService implements ElectorRoleService {

        private  final Logger               LOG = LoggerFactory.getLogger("META-ELECTOR", "[Leader]");

        @Autowired
        private                  DistributeLockMapper distributeLockMapper;

        @Override
        public DistributeLockDomain onWorking(DistributeLockDomain lock, String myself) {

            try {
                /**
                 * as leader, do heartbeat
                 */
                distributeLockMapper.ownerHeartbeat(lock);
                if (LOG.isInfoEnabled()) {
                    LOG.info("leader heartbeat: {}", myself);
                }
                return distributeLockMapper.queryDistLock(lock.getDataCenter(), lock.getLockName());
            } catch (Throwable t) {
                LOG.error("leader:{} heartbeat error.", myself, t);
            }
            return lock;

        }

        @Override
        public ElectorRole support() {
            return ElectorRole.LEADER;
        }
    }

    public class FollowService implements ElectorRoleService {

        private final Logger LOG = LoggerFactory.getLogger("META-ELECTOR",
                "[FOLLOW]");

        @Autowired
        private DistributeLockMapper distributeLockMapper;

        @Override
        public DistributeLockDomain onWorking(DistributeLockDomain lock, String myself) {
            /**
             * as follow, do compete if lock expire
             */
            if (lock.expire()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("lock expire: {}, meta elector start: {}", lock, myself);
                }
                distributeLockMapper.competeLockOnUpdate(new FollowCompeteLockDomain(lock.getDataCenter(),
                        lock.getLockName(), lock.getOwner(), lock.getGmtModified(), myself));
                DistributeLockDomain newLock = distributeLockMapper.queryDistLock(lock.getDataCenter(), lock.getLockName());
                if (LOG.isInfoEnabled()) {
                    LOG.info("elector finish, new lock: {}", lock);
                }
                return newLock;
            }
            return lock;
        }

        @Override
        public ElectorRole support() {
            return ElectorRole.FOLLOWER;
        }
    }

    public void addElectorRoleService(ElectorRoleService service) {
        electorRoleMap.put(service.support(), service);
    }

    /**
     * Setter method for property <tt>distributeLockMapper</tt>.
     *
     * @param distributeLockMapper value to be assigned to property distributeLockMapper
     */
    public void setDistributeLockMapper(DistributeLockMapper distributeLockMapper) {
        this.distributeLockMapper = distributeLockMapper;
    }

    /**
     * Setter method for property <tt>metaElectorConfig</tt>.
     *
     * @param metaElectorConfig value to be assigned to property metaElectorConfig
     */
    public void setMetaElectorConfig(MetaElectorConfig metaElectorConfig) {
        this.metaElectorConfig = metaElectorConfig;
    }
}


