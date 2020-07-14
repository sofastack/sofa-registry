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
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.entity.LeaderChangeContext;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.entity.RaftOutter.SnapshotMeta;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.registry.jraft.bootstrap.ServiceStateMachine;
import com.alipay.sofa.registry.jraft.command.ProcessRequest;
import com.alipay.sofa.registry.jraft.command.ProcessResponse;
import com.alipay.sofa.registry.jraft.processor.*;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author shangyu.wh
 * @version 1.0: TestServiceStateMachine.java, v 0.1 2019-08-01 15:16 shangyu.wh Exp $
 */
public class TestServiceStateMachine {

    @Test
    public void testApply() {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();

        Processor processor = Processor.getInstance();

        processor.addWorker(TestServiceStateMachine.class.getSimpleName(),
            TestServiceStateMachine.class, new TestServiceStateMachine());

        AtomicInteger count = new AtomicInteger();

        LeaderTaskClosure leaderTaskClosure = new LeaderTaskClosure();
        ProcessRequest processRequest = new ProcessRequest();
        processRequest
            .setMethodArgs(new Object[] { TestServiceStateMachine.class.getSimpleName() });
        processRequest.setMethodArgSigs(new String[] { "java.lang.String" });
        processRequest.setMethodName("testMethod");
        processRequest.setServiceName(TestServiceStateMachine.class.getSimpleName());
        leaderTaskClosure.setRequest(processRequest);

        serviceStateMachine.onApply(new Iterator() {

            @Override
            public boolean hasNext() {
                if (count.get() > 0)
                    return false;
                return true;
            }

            @Override
            public ByteBuffer next() {
                count.getAndIncrement();
                return null;
            }

            @Override
            public ByteBuffer getData() {
                return ByteBuffer.allocate(10);
            }

            @Override
            public long getIndex() {
                return 0;
            }

            @Override
            public long getTerm() {
                return 0;
            }

            @Override
            public Closure done() {
                return leaderTaskClosure;
            }

            @Override
            public void setErrorAndRollback(long ntail, Status st) {

            }
        });

        Assert.assertEquals(TestServiceStateMachine.class.getSimpleName(),
            ((ProcessResponse) leaderTaskClosure.getResponse()).getEntity());
    }

    @Test
    public void testApply2() {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();

        Processor processor = Processor.getInstance();

        processor.addWorker(TestServiceStateMachine.class.getSimpleName(),
            TestServiceStateMachine.class, new TestServiceStateMachine());

        AtomicInteger count = new AtomicInteger();

        LeaderTaskClosure leaderTaskClosure = new LeaderTaskClosure();
        ProcessRequest processRequest = new ProcessRequest();
        processRequest
            .setMethodArgs(new Object[] { TestServiceStateMachine.class.getSimpleName() });
        processRequest.setMethodArgSigs(new String[] { "java.lang.String" });
        processRequest.setMethodName("testMethod");
        processRequest.setServiceName(TestServiceStateMachine.class.getSimpleName());
        leaderTaskClosure.setRequest(processRequest);

        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Hessian2Output hessianOutput = new Hessian2Output(byteStream);
        SerializerFactory serializerFactory = new SerializerFactory();
        hessianOutput.setSerializerFactory(serializerFactory);
        try {
            hessianOutput.writeObject(processRequest);
            hessianOutput.close();
        } catch (IOException e) {
        }

        serviceStateMachine.onApply(new Iterator() {

            @Override
            public boolean hasNext() {
                if (count.get() > 0)
                    return false;
                return true;
            }

            @Override
            public ByteBuffer next() {
                count.getAndIncrement();
                return null;
            }

            @Override
            public ByteBuffer getData() {
                byte[] cmdBytes = byteStream.toByteArray();

                ByteBuffer data = ByteBuffer.allocate(cmdBytes.length);
                data.put(cmdBytes);
                data.flip();
                return data;
            }

            @Override
            public long getIndex() {
                return 0;
            }

            @Override
            public long getTerm() {
                return 0;
            }

            @Override
            public Closure done() {
                return null;
            }

            @Override
            public void setErrorAndRollback(long ntail, Status st) {

            }
        });

    }

    @Test
    public void testOnSnapshotSave() throws InterruptedException {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();
        Processor processor = Processor.getInstance();
        LeaderTaskClosure leaderTaskClosure = new LeaderTaskClosure();
        AtomicReference<Status> status = new AtomicReference<>();
        leaderTaskClosure.setDone(statusIn -> {
            status.set(statusIn);
        });

        SnapshotProcess process = new SnapshotProcess() {

            @Override
            public boolean save(String path) {
                return true;
            }

            @Override
            public boolean load(String path) {
                return true;
            }

            @Override
            public SnapshotProcess copy() {
                return this;
            }

            @Override
            public Set<String> getSnapshotFileNames() {
                return new HashSet<>(Arrays.asList(new String[] { "aaa", "sss" }));
            }
        };
        processor.addWorker(process.getClass().getSimpleName(), process.getClass(), process);

        serviceStateMachine.onSnapshotSave(new SnapshotWriter() {

            @Override
            public void close() throws IOException {

            }

            @Override
            public String getPath() {
                return "path";
            }

            @Override
            public Set<String> listFiles() {
                return null;
            }

            @Override
            public Message getFileMeta(String fileName) {
                return null;
            }

            @Override
            public boolean init(Void opts) {
                return false;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public boolean saveMeta(SnapshotMeta meta) {
                return false;
            }

            public boolean addFile(String fileName) {
                return true;
            }

            @Override
            public boolean addFile(String fileName, Message fileMeta) {
                return true;
            }

            @Override
            public boolean removeFile(String fileName) {
                return false;
            }

            @Override
            public void close(boolean keepDataOnError) throws IOException {

            }
        }, leaderTaskClosure);

        TimeUnit.MILLISECONDS.sleep(1000);

        Assert.assertTrue(status.get().isOk());
    }

    @Test
    public void testOnSnapshotLoad() throws InterruptedException {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();
        Processor processor = Processor.getInstance();
        SnapshotProcess process = new SnapshotProcess() {

            @Override
            public boolean save(String path) {
                return true;
            }

            @Override
            public boolean load(String path) {
                return true;
            }

            @Override
            public SnapshotProcess copy() {
                return this;
            }

            @Override
            public Set<String> getSnapshotFileNames() {
                return new HashSet<>(Arrays.asList(new String[] { "aaa", "sss" }));
            }
        };
        processor.addWorker(process.getClass().getSimpleName(), process.getClass(), process);

        boolean ret = serviceStateMachine.onSnapshotLoad(new SnapshotReader() {

            @Override
            public void close() throws IOException {

            }

            @Override
            public String getPath() {
                return "Path";
            }

            @Override
            public Set<String> listFiles() {
                return null;
            }

            @Override
            public Message getFileMeta(String fileName) {
                return new Message() {
                    @Override
                    public Message getDefaultInstanceForType() {
                        return null;
                    }

                    @Override
                    public boolean isInitialized() {
                        return false;
                    }

                    @Override
                    public List<String> findInitializationErrors() {
                        return null;
                    }

                    @Override
                    public String getInitializationErrorString() {
                        return null;
                    }

                    @Override
                    public Descriptor getDescriptorForType() {
                        return null;
                    }

                    @Override
                    public Map<FieldDescriptor, Object> getAllFields() {
                        return null;
                    }

                    @Override
                    public boolean hasOneof(OneofDescriptor oneofDescriptor) {
                        return false;
                    }

                    @Override
                    public FieldDescriptor getOneofFieldDescriptor(OneofDescriptor oneofDescriptor) {
                        return null;
                    }

                    @Override
                    public boolean hasField(FieldDescriptor fieldDescriptor) {
                        return false;
                    }

                    @Override
                    public Object getField(FieldDescriptor fieldDescriptor) {
                        return null;
                    }

                    @Override
                    public int getRepeatedFieldCount(FieldDescriptor fieldDescriptor) {
                        return 0;
                    }

                    @Override
                    public Object getRepeatedField(FieldDescriptor fieldDescriptor, int i) {
                        return null;
                    }

                    @Override
                    public UnknownFieldSet getUnknownFields() {
                        return null;
                    }

                    @Override
                    public void writeTo(CodedOutputStream codedOutputStream) throws IOException {

                    }

                    @Override
                    public int getSerializedSize() {
                        return 0;
                    }

                    @Override
                    public Parser<? extends Message> getParserForType() {
                        return null;
                    }

                    @Override
                    public ByteString toByteString() {
                        return null;
                    }

                    @Override
                    public byte[] toByteArray() {
                        return new byte[0];
                    }

                    @Override
                    public void writeTo(OutputStream outputStream) throws IOException {

                    }

                    @Override
                    public void writeDelimitedTo(OutputStream outputStream) throws IOException {

                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public String toString() {
                        return null;
                    }

                    @Override
                    public Builder newBuilderForType() {
                        return null;
                    }

                    @Override
                    public Builder toBuilder() {
                        return null;
                    }
                };
            }

            @Override
            public boolean init(Void opts) {
                return false;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public SnapshotMeta load() {
                return null;
            }

            @Override
            public String generateURIForCopy() {
                return null;
            }
        });

        TimeUnit.MILLISECONDS.sleep(1000);

        Assert.assertTrue(ret);
    }

    @Test
    public void testRemain() throws InterruptedException {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();

        AtomicInteger leaderstart = new AtomicInteger();
        AtomicInteger leaderstop = new AtomicInteger();

        serviceStateMachine.setFollowerProcessListener(new FollowerProcessListener() {
            @Override
            public void startProcess(PeerId leader) {
                leaderstop.getAndIncrement();
            }

            @Override
            public void stopProcess(PeerId leader) {
                leaderstop.getAndIncrement();
            }
        });

        serviceStateMachine.setLeaderProcessListener(new LeaderProcessListener() {
            @Override
            public void startProcess() {
                leaderstart.getAndIncrement();
            }

            @Override
            public void stopProcess() {
                leaderstart.getAndIncrement();
            }
        });

        serviceStateMachine.onLeaderStart(1);
        TimeUnit.MILLISECONDS.sleep(500);
        Assert.assertEquals(leaderstart.get(), 1);

        serviceStateMachine.onLeaderStop(Status.OK());
        TimeUnit.MILLISECONDS.sleep(500);
        Assert.assertTrue(leaderstart.get() == 2);

        serviceStateMachine.onStartFollowing(new LeaderChangeContext(new PeerId(), 1, Status.OK()));
        TimeUnit.MILLISECONDS.sleep(500);
        Assert.assertTrue(leaderstop.get() == 1);

        serviceStateMachine.onStopFollowing(new LeaderChangeContext(new PeerId(), 1, Status.OK()));
        TimeUnit.MILLISECONDS.sleep(500);
        Assert.assertTrue(leaderstop.get() == 2);

    }

    public String testMethod(String ss) {
        return ss;
    }

    @Test
    public void TestOnConfChange() throws InterruptedException {
        ServiceStateMachine serviceStateMachine = ServiceStateMachine.getInstance();
        AtomicInteger confChanged = new AtomicInteger();
        serviceStateMachine
            .setConfigurationCommittedListener(conf -> confChanged.getAndIncrement());
        serviceStateMachine.onConfigurationCommitted(JRaftUtils.getConfiguration("127.0.0.1"));
        TimeUnit.MILLISECONDS.sleep(500);
        Assert.assertEquals(confChanged.get(), 1);
    }
}