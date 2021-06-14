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

import com.alipay.sofa.registry.util.StringFormatter;
import java.util.concurrent.atomic.LongAdder;

public final class HackAsyncLoggerDisruptor extends AsyncLoggerDisruptor {
  private final LongAdder fullCount = new LongAdder();

  HackAsyncLoggerDisruptor(String contextName) {
    super(contextName);
  }

  @Override
  boolean tryPublish(final RingBufferLogEventTranslator translator) {
    boolean published = super.tryPublish(translator);
    if (!published) {
      fullCount.increment();
    }
    return published;
  }

  long fullCountAndReset() {
    return fullCount.sumThenReset();
  }

  @Override
  public String toString() {
    return StringFormatter.format(
        "HackAsyncLoggerDisruptor{ctx={},{}}", getContextName(), super.toString());
  }
}
