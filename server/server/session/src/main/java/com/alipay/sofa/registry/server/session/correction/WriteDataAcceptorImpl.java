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
package com.alipay.sofa.registry.server.session.correction;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author shangyu.wh
 * @version 1.0: WriteDataAcceptor.java, v 0.1 2019-06-06 12:45 shangyu.wh Exp $
 */
public class WriteDataAcceptorImpl implements WriteDataAcceptor {

    private static final Logger             LOGGER              = LoggerFactory
                                                                    .getLogger(WriteDataAcceptorImpl.class);

    /**
     * acceptor for all write data request
     * key:connectId
     * value:writeRequest processor
     *
     */
    private Map<String, WriteDataProcessor> writeDataProcessors = new ConcurrentHashMap();

    public void accept(WriteDataRequest request){

        String connectId = request.getConnectId();
        WriteDataProcessor writeDataProcessor = writeDataProcessors.computeIfAbsent(connectId,
                key -> new WriteDataProcessor(connectId));

        writeDataProcessor.process(request);
    }

    public boolean halt(String connectId) {

        WriteDataProcessor writeDataProcessor = writeDataProcessors.get(connectId);
        if (writeDataProcessor != null) {
            return writeDataProcessor.halt();
        }
        return false;
    }

    public void resume(String connectId) {
        WriteDataProcessor writeDataProcessor = writeDataProcessors.get(connectId);
        if (writeDataProcessor != null) {
            writeDataProcessor.resume();
        }
    }

    public void remove(String connectId) {
        writeDataProcessors.remove(connectId);
    }
}