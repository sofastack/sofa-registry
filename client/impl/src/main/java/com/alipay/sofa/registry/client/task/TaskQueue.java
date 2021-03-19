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
import com.alipay.sofa.registry.client.provider.AbstractInternalRegister;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Task queue.
 *
 * @author zhuoyu.sjw
 * @version $Id : TaskQueue.java, v 0.1 2018-02-28 20:44 zhuoyu.sjw Exp $$
 */
public class TaskQueue implements Iterable<TaskEvent> {

  private final ConcurrentMap<String, TaskEvent> taskMap =
      new ConcurrentHashMap<String, TaskEvent>();

  /**
   * Iterator iterator.
   *
   * @return the iterator
   */
  @Override
  public Iterator<TaskEvent> iterator() {
    List<TaskEvent> taskList = new ArrayList<TaskEvent>(taskMap.values());
    Collections.sort(taskList);
    return taskList.iterator();
  }

  /** Delete the completed task, return task queue size. */
  public void cleanCompletedTasks() {
    List<String> taskList = new ArrayList<String>(taskMap.keySet());
    for (String key : taskList) {
      TaskEvent event = taskMap.get(key);
      AbstractInternalRegister r = (AbstractInternalRegister) event.getSource();
      if (r.isDone()) {
        taskMap.remove(key, event);
      }
    }
    taskMap.size();
  }

  /**
   * Add task event to task queue.
   *
   * @param event task event
   */
  public void put(TaskEvent event) {
    Register register = event.getSource();
    String key = register.getRegistId();
    taskMap.put(key, event);
  }

  /**
   * Put all.
   *
   * @param taskEvents the task events
   */
  public void putAll(List<TaskEvent> taskEvents) {
    for (TaskEvent event : taskEvents) {
      put(event);
    }
  }

  /**
   * Is empty boolean.
   *
   * @return the boolean
   */
  public boolean isEmpty() {
    return taskMap.isEmpty();
  }
}
