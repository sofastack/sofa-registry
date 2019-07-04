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
package com.alipay.sofa.registry.server.data.datasync.sync;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.dataserver.SyncData;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.datasync.Operator;
import com.alipay.sofa.registry.util.DatumVersionUtil;

/**
 *
 * @author shangyu.wh
 * @version $Id: Acceptor.java, v 0.1 2018-03-05 16:57 shangyu.wh Exp $
 */
public class Acceptor {

    static final int                        DEFAULT_DURATION_SECS = 30;
    private static final Logger             LOGGER                = LoggerFactory.getLogger(
                                                                      Acceptor.class,
                                                                      "[SyncDataService]");
    private final Deque<Long/*version*/>   logOperatorsOrder     = new ConcurrentLinkedDeque<>();
    private final String                    dataInfoId;
    private final String                    dataCenter;
    private int                             maxBufferSize;
    private Map<Long/*version*/, Operator> logOperators          = new ConcurrentHashMap<>();

    private final ReentrantReadWriteLock    readWriteLock         = new ReentrantReadWriteLock();
    private final Lock                      read                  = readWriteLock.readLock();
    private final Lock                      write                 = readWriteLock.writeLock();

    private final DatumCache                datumCache;

    /**
     * constructor
     * @param maxBufferSize
     * @param dataInfoId
     * @param dataCenter
     */
    public Acceptor(int maxBufferSize, String dataInfoId, String dataCenter, DatumCache datumCache) {
        this.maxBufferSize = maxBufferSize;
        this.dataInfoId = dataInfoId;
        this.dataCenter = dataCenter;
        this.datumCache = datumCache;
    }

    /**
     * append operator to queue,if queue is full poll the first element and append.
     * Process will check version sequence,it must append with a consequent increase in version,
     * otherwise queue will be clean
     *
     * @param operator
     */
    public void appendOperator(Operator operator) {
        write.lock();
        try {
            if (isFull()) {
                logOperators.remove(logOperatorsOrder.poll());
            }
            if (operator.getSourceVersion() == null) {
                operator.setSourceVersion(0L);
            }
            Long tailVersion = logOperatorsOrder.peekLast();

            if (tailVersion != null) {
                //operation add not by solid sequence
                if (tailVersion.longValue() != operator.getSourceVersion().longValue()) {
                    LOGGER
                        .warn(
                            "Datum {} append operation not follow version sequence!Current version {},but input source version {}.In order to avoid get "
                                    + "data error clear operations!", operator.getDatum()
                                .getDataInfoId(), tailVersion, operator.getSourceVersion());
                    clearBefore();
                }
            }

            Operator previousOperator = logOperators.put(operator.getVersion(), operator);
            if (previousOperator == null) {
                logOperatorsOrder.add(operator.getVersion());
            } else {
                LOGGER.warn("Append operation has been exist!");
            }
        } finally {
            write.unlock();
        }
    }

    /**
     *
     * @return
     */
    public Collection<Operator> getAllOperators() {
        LinkedList<Operator> linkRet = new LinkedList<>();
        if (logOperatorsOrder.peek() != null) {
            Iterator iterator = logOperatorsOrder.iterator();
            while (iterator.hasNext()) {
                Long i = (Long) iterator.next();
                linkRet.add(logOperators.get(i));
            }
        }
        return linkRet;
    }

    /**
     * process send data current version,check current version exist in current queue
     * if(existed) return operators which version bigger than current version
     * else get all datum data operators
     *
     * @param currentVersion
     * @return
     */
    public SyncData process(Long currentVersion) {
        read.lock();
        try {
            Collection<Operator> operators = acceptOperator(currentVersion);

            List<Datum> retList = new LinkedList<>();
            SyncData syncData;
            boolean wholeDataTag = false;
            if (operators != null) {
                //first get all data
                if (operators.isEmpty()) {
                    wholeDataTag = true;
                    retList.add(datumCache.get(dataCenter, dataInfoId));
                    LOGGER.info("Get all data!dataInfoID:{} dataCenter:{}.All data size{}:",
                        dataInfoId, dataCenter, retList.size());
                } else {
                    for (Operator operator : operators) {
                        retList.add(operator.getDatum());
                    }
                }
                syncData = new SyncData(dataInfoId, dataCenter, wholeDataTag, retList);
            } else {
                //no match get all data
                LOGGER
                    .info(
                        "Append log queue is empty,Maybe all logs record expired or no operator append!So must get all data!dataInfoID:{} dataCenter:{}.queue size{}:",
                        dataInfoId, dataCenter, logOperatorsOrder.size());
                wholeDataTag = true;
                retList.add(datumCache.get(dataCenter, dataInfoId));
                syncData = new SyncData(dataInfoId, dataCenter, wholeDataTag, retList);
            }

            return syncData;
        } finally {
            read.unlock();
        }
    }

    /**
     *
     * @param currentVersion
     * @return
     */
    public Collection<Operator> acceptOperator(Long currentVersion) {
        //first get all data
        if (currentVersion == null) {
            LOGGER
                .info(
                    "Current version input is null,maybe first get all data!dataInfoID:{} dataCenter:{}",
                    dataInfoId, dataCenter);
            return new ArrayList<>();
        }

        if (logOperatorsOrder.size() > 0) {
            LinkedList<Operator> linkRet = new LinkedList<>();
            //target version found
            if (logOperatorsOrder.contains(currentVersion)) {
                Iterator iterator = logOperatorsOrder.descendingIterator();
                while (iterator.hasNext()) {
                    Long i = (Long) iterator.next();
                    if (currentVersion.equals(i)) {
                        break;
                    }
                    linkRet.addFirst(logOperators.get(i));
                }
            } else {
                //target version not found,but source version equals
                Operator headOperator = logOperators.get(logOperatorsOrder.peek());
                if (currentVersion.equals(headOperator.getSourceVersion())) {
                    LOGGER
                        .info("current version not found on queue,but header source version equals current version!");
                    linkRet.addAll(logOperators.values());
                }
            }
            if (linkRet.isEmpty()) {
                LOGGER.info("Current version {} not match on queue,queue size {} !",
                    currentVersion, logOperatorsOrder.size());
            }
            return linkRet;
        }
        //cannot match version,must poll all data
        return null;
    }

    /**
     *
     * @param durationSEC
     */
    public void checkExpired(int durationSEC) {
        write.lock();
        try {
            //check all expired
            Long peekVersion = logOperatorsOrder.peek();
            if (peekVersion != null && isExpired(durationSEC, peekVersion)) {
                logOperators.remove(logOperatorsOrder.poll());
                checkExpired(durationSEC);
            }
        } finally {
            write.unlock();
        }
    }

    /**
     *
     * @return
     */
    public Long getLastVersion() {
        return logOperatorsOrder.peekLast();
    }

    private boolean isFull() {
        return logOperators.size() >= maxBufferSize;
    }

    private boolean isExpired(int durationSECS, long peekVersion) {
        durationSECS = (durationSECS > 0) ? durationSECS * 1000 : DEFAULT_DURATION_SECS * 1000;
        boolean ret = System.currentTimeMillis() > DatumVersionUtil.getRealTimestamp(peekVersion)
                                                   + durationSECS;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("now:" + System.currentTimeMillis() + " peek:" + peekVersion + " du:"
                         + durationSECS + " result:" + ret);
        }
        return ret;
    }

    private void clearBefore() {
        write.lock();
        try {

            logOperators.clear();
            logOperatorsOrder.clear();
        } finally {
            write.unlock();
        }
    }

    /**
     *
     */
    public void printInfo() {
        LOGGER
            .debug("----------------------------------------------------------------------------");
        LOGGER.debug("Acceptor info has " + logOperators.size() + " operations，dataInfoID:"
                     + this.getDataInfoId() + " dataCenter:" + this.getDataCenter());
        if (logOperatorsOrder.size() > 0) {
            for (Long version : logOperatorsOrder) {
                Operator operator = logOperators.get(version);
                LOGGER.debug("| " + pidLine(String.valueOf(operator.getVersion()), 24, " ")
                             + pidLine(String.valueOf(operator.getSourceVersion()), 24, " ")
                             + operator.getDatum());

            }
        }
    }

    private String pidLine(String line, int length, String space) {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        int spaces = length - line.getBytes(Charset.defaultCharset()).length - 1;
        for (int i = 0; i < spaces; i++) {
            sb.append(space);
        }
        return sb.toString();
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     *
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
        return dataInfoId;
    }

    /**
     * Getter method for property <tt>dataCenter</tt>.
     *
     * @return property value of dataCenter
     */
    public String getDataCenter() {
        return dataCenter;
    }

}