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
package com.alipay.sofa.registry.server.session.listener;

import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;
import com.alipay.sofa.registry.server.session.TestUtils;
import com.alipay.sofa.registry.server.session.node.processor.MetaNodeSingleTaskProcessor;
import com.alipay.sofa.registry.task.batcher.TaskDispatchers;
import com.alipay.sofa.registry.task.listener.TaskEvent;
import org.junit.Assert;
import org.junit.Test;

public class ProvideDataChangeFetchTaskListenerTest {
  @Test
  public void test() {
    MetaNodeSingleTaskProcessor processor = new MetaNodeSingleTaskProcessor();
    ProvideDataChangeFetchTaskListener listener = new ProvideDataChangeFetchTaskListener(processor);
    listener.sessionServerConfig = TestUtils.newSessionConfig("testDc");
    listener.init();
    Assert.assertEquals(listener.support(), TaskEvent.TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK);
    ProvideDataChangeEvent provideDataChangeEvent =
        new ProvideDataChangeEvent("testProvideDataId", 100);
    TaskEvent taskEvent =
        new TaskEvent(provideDataChangeEvent, TaskEvent.TaskType.PROVIDE_DATA_CHANGE_FETCH_TASK);
    listener.handleEvent(taskEvent);

    Assert.assertNotNull(TaskDispatchers.getDispatcherName("test"));
    Assert.assertEquals(TaskDispatchers.getTaskDispatcherMap().size(), 1);
    TaskDispatchers.createSingleTaskDispatcher("test", 10, 10, 10, 10, processor);
    Assert.assertEquals(TaskDispatchers.getTaskDispatcherMap().size(), 2);
    TaskDispatchers.stopDefaultSingleTaskDispatcher();
  }
}
