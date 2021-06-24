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
package org.apache.logging.log4j.core.async;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.log.SLF4JLogger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.Assert;

public class HackTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HackTest.class);

  public void testSLF4jLogger() {
    Assert.assertTrue(LOGGER instanceof SLF4JLogger);
    Assert.assertEquals(LOGGER.getName(), HackTest.class.getName());
    Assert.assertTrue(LOGGER.toString(), LOGGER.toString().contains(HackTest.class.getName()));
  }

  public void test() throws Exception {
    final int num = Hack.printDisruptorStats();
    Assert.assertNull(Hack.hackLoggerDisruptor(null));
    Assert.assertEquals(num, Hack.printDisruptorStats());

    Assert.assertEquals(LOGGER, Hack.hackLoggerDisruptor(LOGGER));
    Assert.assertEquals(num, Hack.printDisruptorStats());
    AsyncLoggerDisruptor loggerDisruptor = new AsyncLoggerDisruptor("testCtx");
    AsyncLogger asyncLogger =
        new AsyncLogger(new LoggerContext("testCtx"), "testCtx", null, loggerDisruptor);
    Assert.assertTrue(Hack.FIELD_DISRUPTOR.get(asyncLogger) == loggerDisruptor);

    Hack.hackLoggerDisruptor(LOGGER, asyncLogger);
    HackAsyncLoggerDisruptor hack =
        (HackAsyncLoggerDisruptor) Hack.FIELD_DISRUPTOR.get(asyncLogger);
    Assert.assertNotNull(hack);
    Assert.assertTrue(hack != loggerDisruptor);
    Assert.assertTrue(
        hack.toString(), hack.toString().contains(HackAsyncLoggerDisruptor.class.getSimpleName()));
    Assert.assertEquals(num + 1, Hack.printDisruptorStats());
  }
}
