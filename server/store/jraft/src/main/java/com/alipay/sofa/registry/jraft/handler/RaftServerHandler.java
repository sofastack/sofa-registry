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
package com.alipay.sofa.registry.jraft.handler;

import com.alipay.remoting.AsyncContext;
import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.registry.jraft.bootstrap.RaftServer;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.jraft.command.ProcessResponse;
import com.alipay.sofa.registry.jraft.processor.LeaderTaskClosure;
import com.alipay.sofa.registry.jraft.processor.Processor;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.remoting.bolt.BoltChannel;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 *
 * @author shangyu.wh
 * @version $Id: ServerHandler.java, v 0.1 2017-11-28 18:06 shangyu.wh Exp $
 */
public class RaftServerHandler implements ChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftServerHandler.class);

    protected RaftServer        raftServer;

    /**
     * constructor
     * @param raftServer
     */
    public RaftServerHandler(RaftServer raftServer) {
        this.raftServer = raftServer;
    }

    @Override
    public void connected(Channel channel) {
    }

    @Override
    public void disconnected(Channel channel) {
    }

    @Override
    public void caught(Channel channel, Object message, Throwable exception) {
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return ProcessRequest.class;
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

        if (!(channel instanceof BoltChannel)) {
            LOGGER.error("Raft receive message channel error type!");
            throw new RemotingException("Raft receive message channel error type!");
        }

        if (!(message instanceof ProcessRequest)) {
            LOGGER.error("Raft receive message error type!");
            throw new RemotingException("Raft receive message error type!");
        }

        BoltChannel boltChannel = (BoltChannel) channel;
        AsyncContext asyncContext = boltChannel.getAsyncContext();

        if (!raftServer.getFsm().isLeader()) {
            asyncContext.sendResponse(ProcessResponse.redirect(raftServer.redirect()).build());
            return;
        }
        ProcessRequest processRequest = (ProcessRequest) message;

        long start = System.currentTimeMillis();

        Method method = Processor.getInstance().getWorkMethod(processRequest);

        if (Processor.getInstance().isLeaderReadMethod(method)) {
            Object obj = Processor.getInstance().process(method, processRequest);
            long cost = System.currentTimeMillis() - start;
            LOGGER.info("Raft server process request self cost:{},request={}", cost, processRequest);
            asyncContext.sendResponse(obj);
        } else {
            LeaderTaskClosure closure = new LeaderTaskClosure();
            closure.setRequest(processRequest);
            closure.setDone(status -> {
                long cost = System.currentTimeMillis() - start;
                LOGGER.info("Raft server process request by task cost:{},request={},status={}", cost, processRequest,
                        status);
                if (status.isOk()) {
                    asyncContext.sendResponse(closure.getResponse());
                } else {
                    asyncContext.sendResponse(ProcessResponse.fail(status.getErrorMsg()).build());
                }
            });

            Task task = createTask(closure, processRequest);

            raftServer.getNode().apply(task);
        }
    }

    protected Task createTask(LeaderTaskClosure closure, ProcessRequest request) {

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Hessian2Output hessianOutput = new Hessian2Output(byteStream);
        SerializerFactory serializerFactory = new SerializerFactory();
        hessianOutput.setSerializerFactory(serializerFactory);
        try {
            hessianOutput.writeObject(request);
            hessianOutput.close();
        } catch (IOException e) {
            LOGGER.error("Raft receive message serialize error!", e);
        }

        byte[] cmdBytes = byteStream.toByteArray();

        ByteBuffer data = ByteBuffer.allocate(cmdBytes.length);
        data.put(cmdBytes);
        data.flip();
        return new Task(data, closure);
    }

    @Override
    public Object reply(Channel channel, Object message) {
        return null;
    }

    @Override
    public InvokeType getInvokeType() {
        return InvokeType.ASYNC;
    }
}