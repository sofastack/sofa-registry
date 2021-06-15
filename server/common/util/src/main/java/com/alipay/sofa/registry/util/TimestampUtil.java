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
package com.alipay.sofa.registry.util;

import java.sql.Timestamp;

/**
 * @author xiaojian.xj
 * @version $Id: TimestampUtil.java, v 0.1 2021年03月01日 15:55 xiaojian.xj Exp $
 */
public final class TimestampUtil {
  private TimestampUtil() {}

  public static long getNanosLong(Timestamp timestamp) {
    return timestamp.getTime() / 1000 * 1000000000 + timestamp.getNanos();
  }

  public static Timestamp fromNanosLong(long nanos) {
    Timestamp ts = new Timestamp(nanos / 1000000);
    ts.setNanos((int) (nanos % 1000000000));
    return ts;
  }
}
