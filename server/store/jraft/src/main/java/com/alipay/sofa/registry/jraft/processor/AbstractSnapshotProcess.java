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

import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author shangyu.wh
 * @version $Id: AbstractSnapshotProcess.java, v 0.1 2018-05-29 12:12 shangyu.wh Exp $
 */
public abstract class AbstractSnapshotProcess implements SnapshotProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSnapshotProcess.class);

    /**
     * save snapshot to file
     * @param path
     * @param values
     * @return
     */
    public boolean save(String path, Object values) {
        try {
            FileUtils.writeByteArrayToFile(new File(path), CommandCodec.encodeCommand(values),
                false);
            return true;
        } catch (IOException e) {
            LOGGER.error("Fail to save snapshot", e);
            return false;
        }
    }

    /**
     * load snapshot from file
     * @param path
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> T load(String path, Class<T> clazz) throws IOException {
        byte[] bs = FileUtils.readFileToByteArray(new File(path));
        if (bs != null && bs.length > 0) {
            return CommandCodec.decodeCommand(bs, clazz);
        }
        throw new IOException("Fail to load snapshot from " + path + ", content: "
                              + Arrays.toString(bs));
    }

}