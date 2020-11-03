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
package com.alipay.sofa.registry.server.meta.remoting.handler;

import com.alipay.sofa.registry.common.model.metaserver.FetchRevisionsRequest;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.meta.revision.AppRevisionRegistry;
import org.springframework.beans.factory.annotation.Autowired;

public class FetchRevisionsHandler extends AbstractServerHandler<FetchRevisionsRequest> {
    @Autowired
    private AppRevisionRegistry appRevisionRegistry;

    @Override
    public Object reply(Channel channel, FetchRevisionsRequest message) {
        return appRevisionRegistry.fetchRevisions(message.keys);
    }

    @Override
    public HandlerType getType() {
        return HandlerType.PROCESSER;
    }

    @Override
    public Class interest() {
        return FetchRevisionsRequest.class;
    }
}
