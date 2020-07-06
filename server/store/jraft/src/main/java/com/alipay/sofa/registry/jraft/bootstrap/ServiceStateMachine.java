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
package com.alipay.sofa.registry.jraft.bootstrap;

import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.jraft.util.Utils;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.jraft.command.ProcessResponse;
import com.alipay.sofa.registry.jraft.processor.*;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author shangyu.wh
 * @version $Id: ServiceStateMachine.java, v 0.1 2018-05-21 15:54 shangyu.wh Exp $
 */
public class ServiceStateMachine extends StateMachineAdapter {

    private static final Logger                 LOG = LoggerFactory
                                                        .getLogger(ServiceStateMachine.class);

    private LeaderProcessListener               leaderProcessListener;

    private FollowerProcessListener             followerProcessListener;

    private ConfigurationCommittedListener      configurationCommittedListener;

    private static volatile ServiceStateMachine instance;

    /**
     * get instance of ServiceStateMachine
     * @return
     */
    public static ServiceStateMachine getInstance() {
        if (instance == null) {
            synchronized (ServiceStateMachine.class) {
                if (instance == null) {
                    instance = new ServiceStateMachine();
                }
            }
        }
        return instance;
    }

    /**
     * leader term
     */
    private AtomicLong leaderTerm   = new AtomicLong(-1);

    /**
     * follower term
     */
    private AtomicLong followerTerm = new AtomicLong(-1);

    /**
     * verify is leader or not
     * @return
     */
    public boolean isLeader() {
        return this.leaderTerm.get() > 0;
    }

    /**
     * verify is follower or not
     * @return
     */
    public boolean isfollower() {
        return this.followerTerm.get() > 0;
    }

    @Override
    public void onApply(Iterator iter) {
        while (iter.hasNext()) {
            Closure done = iter.done();
            ByteBuffer data = iter.getData();
            ProcessRequest request;
            LeaderTaskClosure closure = null;

            if (done != null) {
                closure = (LeaderTaskClosure) done;
                request = closure.getRequest();
            } else {

                Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(data.array()));
                SerializerFactory serializerFactory = new SerializerFactory();
                input.setSerializerFactory(serializerFactory);
                try {
                    request = (ProcessRequest) input.readObject();
                    input.close();
                } catch (IOException e) {
                    throw new RuntimeException(
                        "IOException occurred when Hessian serializer decode!", e);
                }
            }

            ProcessResponse response = Processor.getInstance().process(request);

            if (closure != null) {
                closure.setResponse(response);
                closure.run(Status.OK());
            }
            iter.next();
        }
    }

    @Override
    public void onSnapshotSave(final SnapshotWriter writer, final Closure done) {

        Map<String, Object> workers = Processor.getInstance().getWorkers();
        Map<String, SnapshotProcess> snapshotProcessors = new HashMap<>();
        if (workers != null) {
            workers.forEach((serviceId, worker) -> {
                if (worker instanceof SnapshotProcess) {
                    SnapshotProcess snapshotProcessor = (SnapshotProcess) worker;
                    snapshotProcessors.put(serviceId, snapshotProcessor.copy());
                }
            });
        }
        Utils.runInThread(() -> {
            String errors = null;
            outer: for (Map.Entry<String, SnapshotProcess> entry : snapshotProcessors.entrySet()) {
                String serviceId = entry.getKey();
                SnapshotProcess snapshotProcessor = entry.getValue();
                Set<String> fileNames = snapshotProcessor.getSnapshotFileNames();
                for (String fileName : fileNames) {
                    String savePath = writer.getPath() + File.separator + fileName;
                    LOG.info("Begin save snapshot path {}", savePath);
                    boolean ret = snapshotProcessor.save(savePath);
                    if (ret) {
                        if (!writer.addFile(fileName)) {
                            errors = String.format("Fail to add file %s to writer", fileName);
                            break outer;
                        }
                    } else {
                        errors = String.format("Fail to save service:%s snapshot %s", serviceId,
                            savePath);
                        break outer;
                    }
                }
            }
            if (errors != null) {
                done.run(new Status(RaftError.EIO, errors));
            } else {
                done.run(Status.OK());
            }
        });

    }

    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (isLeader()) {
            LOG.warn("Leader is not supposed to load snapshot");
            return false;
        }
        List<String> failServices = new ArrayList<>();
        Map<String, Object> workers = Processor.getInstance().getWorkers();
        if (workers != null) {
            outer: for (Map.Entry<String, Object> entry : workers.entrySet()) {
                String serviceId = entry.getKey();
                Object worker = entry.getValue();
                if (worker instanceof SnapshotProcess) {
                    SnapshotProcess snapshotProcess = (SnapshotProcess) worker;
                    Set<String> fileNames = snapshotProcess.getSnapshotFileNames();

                    for (String fileName : fileNames) {
                        if (reader.getFileMeta(fileName) == null) {
                            LOG.error("Fail to find data file {} in {}", fileName, reader.getPath());
                            failServices.add(serviceId);
                            break outer;
                        }

                        String savePath = reader.getPath() + File.separator + fileName;
                        LOG.info("Begin load snapshot path {}", savePath);
                        boolean ret = snapshotProcess.load(savePath);
                        if (!ret) {
                            LOG.error("Fail to load service:{} snapshot {}", serviceId, savePath);
                            failServices.add(serviceId);
                            break outer;
                        }
                    }
                }
            }
        }

        if (!failServices.isEmpty()) {
            LOG.error("Fail to load services {} snapshot!", failServices);
            return false;
        }
        return true;
    }

    @Override
    public void onLeaderStart(long term) {
        this.leaderTerm.set(term);
        if (leaderProcessListener != null) {
            Utils.runInThread(() -> leaderProcessListener.startProcess());
        }
        super.onLeaderStart(term);
    }

    @Override
    public void onLeaderStop(Status status) {
        this.leaderTerm.set(-1);
        if (leaderProcessListener != null) {
            Utils.runInThread(() -> leaderProcessListener.stopProcess());
        }
        super.onLeaderStop(status);
    }

    @Override
    public void onStopFollowing(LeaderChangeContext ctx) {

        this.followerTerm.set(-1);
        if (followerProcessListener != null) {
            Utils.runInThread(() -> followerProcessListener.stopProcess(ctx.getLeaderId()));
        }
        super.onStopFollowing(ctx);
    }

    @Override
    public void onStartFollowing(LeaderChangeContext ctx) {

        this.followerTerm.set(1);
        if (followerProcessListener != null) {
            Utils.runInThread(() -> followerProcessListener.startProcess(ctx.getLeaderId()));
        }
        super.onStartFollowing(ctx);
    }

    @Override
    public void onConfigurationCommitted(Configuration conf) {
        if (configurationCommittedListener != null) {
            Utils.runInThread(() -> configurationCommittedListener.onConfigurationCommitted(conf));
        }
        super.onConfigurationCommitted(conf);
    }

    /**
     * Setter method for property <tt>leaderProcessListener</tt>.
     *
     * @param leaderProcessListener  value to be assigned to property leaderProcessListener
     */
    public void setLeaderProcessListener(LeaderProcessListener leaderProcessListener) {
        this.leaderProcessListener = leaderProcessListener;
    }

    /**
     * Setter method for property <tt>followerProcessListener</tt>.
     *
     * @param followerProcessListener  value to be assigned to property followerProcessListener
     */
    public void setFollowerProcessListener(FollowerProcessListener followerProcessListener) {
        this.followerProcessListener = followerProcessListener;
    }

    public void setConfigurationCommittedListener(ConfigurationCommittedListener listener) {
        this.configurationCommittedListener = listener;
    }
}