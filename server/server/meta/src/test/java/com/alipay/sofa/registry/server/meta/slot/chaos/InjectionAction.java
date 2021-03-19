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
package com.alipay.sofa.registry.server.meta.slot.chaos;

import com.alipay.sofa.registry.common.model.metaserver.nodes.DataNode;
import com.alipay.sofa.registry.server.meta.slot.util.ListUtil;
import java.util.List;
import java.util.Random;
import org.springframework.util.CollectionUtils;

public interface InjectionAction {

  Random random = new Random();

  void doInject(List<DataNode> total, List<DataNode> running);
}

enum InjectionEnum {
  FIRST(0, new FirstInjectionAction()),
  START(70, new StartInjectionAction()),
  STOP(30, new StopInjectionAction()),
  FINAL(0, new FinalInjectionAction()),
  ;

  private int percent;

  private InjectionAction injectionAction;

  public static final Random random = new Random();

  InjectionEnum(int percent, InjectionAction injectionAction) {
    this.percent = percent;
    this.injectionAction = injectionAction;
  }

  public static InjectionEnum pickInject() {
    int percent = random.nextInt(100);
    if (percent < STOP.percent) {
      return STOP;
    } else {
      return START;
    }
  }

  /**
   * Getter method for property <tt>injectionAction</tt>.
   *
   * @return property value of injectionAction
   */
  public InjectionAction getInjectionAction() {
    return injectionAction;
  }
}

class FirstInjectionAction implements InjectionAction {

  @Override
  public void doInject(List<DataNode> total, List<DataNode> running) {
    running.addAll(ListUtil.randomPick(total, 1));
  }
}

class StartInjectionAction implements InjectionAction {

  @Override
  public void doInject(List<DataNode> total, List<DataNode> running) {
    List<DataNode> reduce = ListUtil.reduce(total, running);

    if (CollectionUtils.isEmpty(reduce)) {
      return;
    }

    int count = Math.min(total.size() / 3, random.nextInt(reduce.size()) + 1);
    running.addAll(ListUtil.randomPick(reduce, count));
  }
}

class StopInjectionAction implements InjectionAction {

  @Override
  public void doInject(List<DataNode> total, List<DataNode> running) {
    running.removeAll(ListUtil.randomPick(running, random.nextInt(running.size()) / 3 + 1));
  }
}

class FinalInjectionAction implements InjectionAction {

  @Override
  public void doInject(List<DataNode> total, List<DataNode> running) {
    List<DataNode> reduce = ListUtil.reduce(total, running);
    running.addAll(reduce);
  }
}
