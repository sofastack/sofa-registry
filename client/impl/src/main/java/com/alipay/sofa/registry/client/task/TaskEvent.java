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
package com.alipay.sofa.registry.client.task;

import com.alipay.sofa.registry.client.api.Register;

/**
 * The type Task event.
 *
 * @author zhuoyu.sjw
 * @version $Id : TaskEvent.java, v 0.1 2018-02-28 20:33 zhuoyu.sjw Exp $$
 */
public class TaskEvent implements Comparable<TaskEvent> {
  private static final int MAX_DELAY_TIME = 1000;

  private Register source;

  private int sendCount;

  private long triggerTime;

  /**
   * Constructor.
   *
   * @param source the source
   */
  public TaskEvent(Register source) {
    this.source = source;
    this.triggerTime = System.currentTimeMillis();
    this.sendCount = 0;
  }

  /**
   * Getter method for property <tt>source</tt>.
   *
   * @return property value of source
   */
  public Register getSource() {
    return source;
  }

  /**
   * Setter method for property <tt>triggerTime</tt>.
   *
   * @param triggerTime value to be assigned to property triggerTime
   */
  public void setTriggerTime(long triggerTime) {
    this.triggerTime = triggerTime;
  }

  /**
   * Delay time long.
   *
   * @return long long
   */
  public long delayTime() {
    int time = sendCount * 200;
    if (time > MAX_DELAY_TIME) {
      time = MAX_DELAY_TIME;
    }
    return time - (System.currentTimeMillis() - this.triggerTime);
  }

  /**
   * Compare to int.
   *
   * @param event the event
   * @return the int
   */
  @Override
  public int compareTo(TaskEvent event) {
    if (this.sendCount > event.sendCount) {
      return 1;
    } else if (this.sendCount < event.sendCount) {
      return -1;
    }
    Register register1 = source;
    Register register2 = event.getSource();

    if (register1 == null) {
      return register2 != null ? -1 : 0;
    } else {
      if (register2 == null) {
        return 1;
      }
    }

    long t1 = register1.getTimestamp();
    long t2 = register2.getTimestamp();
    if (t1 > t2) {
      return 1;
    } else if (t1 < t2) {
      return -1;
    }
    return 0;
  }

  /** @see Object#equals(Object) */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TaskEvent)) {
      return false;
    }

    TaskEvent taskEvent = (TaskEvent) o;

    if (sendCount != taskEvent.sendCount) {
      return false;
    }
    if (triggerTime != taskEvent.triggerTime) {
      return false;
    }
    return source != null ? source.equals(taskEvent.source) : taskEvent.source == null;
  }

  /** @see Object#hashCode() */
  @Override
  public int hashCode() {
    int result = source != null ? source.hashCode() : 0;
    result = 31 * result + sendCount;
    result = 31 * result + (int) (triggerTime ^ (triggerTime >>> 32));
    return result;
  }

  /**
   * Inc send count int.
   *
   * @return the int
   */
  public int incSendCount() {
    return this.sendCount++;
  }
}
