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
package com.alipay.sofa.registry.server.session.store;

import com.alipay.sofa.registry.common.model.Tuple;
import com.alipay.sofa.registry.common.model.store.Watcher;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * @author shangyu.wh
 * @version $Id: SessionWatchers.java, v 0.1 2018-04-17 19:00 shangyu.wh Exp $
 */
public class SessionWatchers extends AbstractDataManager<Watcher> implements Watchers {
  private static final Logger LOGGER = LoggerFactory.getLogger(SessionWatchers.class);

  public SessionWatchers() {
    super(LOGGER);
  }

  @Override
  public boolean add(Watcher watcher) {
    Watcher.internWatcher(watcher);
    Tuple<Watcher, Boolean> ret = addData(watcher);
    return ret.o2;
  }

  @Override
  protected int getInitMapSize() {
    return 32;
  }
}
