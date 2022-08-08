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
<<<<<<< HEAD:server/server/meta/src/main/java/com/alipay/sofa/registry/server/meta/slot/SlotBalancer.java
package com.alipay.sofa.registry.server.meta.slot;

import com.alipay.sofa.registry.common.model.slot.SlotTable;

/**
 * @author chen.zhu
 *     <p>Jan 15, 2021
 */
public interface SlotBalancer {

  SlotTable balance();
}
=======
package com.alipay.sofa.registry.server.meta.remoting.session;

import com.alipay.sofa.registry.server.meta.remoting.notifier.Notifier;

/**
 * @author chen.zhu
 *     <p>Feb 23, 2021
 */
public interface SessionServerService extends Notifier {}
>>>>>>> sofastack/master:server/server/meta/src/main/java/com/alipay/sofa/registry/server/meta/remoting/session/SessionServerService.java
