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
package com.alipay.sofa.registry.server.session.predicate;

import com.alipay.sofa.registry.core.model.AppRevisionRegister;
import com.alipay.sofa.registry.server.session.cache.AppRevisionCacheRegistry;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Predicate;

/**
 *
 * @author xiaojian.xj
 * @version $Id: RevisionPredicate.java, v 0.1 2020年11月13日 15:02 xiaojian.xj Exp $
 */
public class RevisionPredicate {

    @Autowired
    private AppRevisionCacheRegistry appRevisionCacheRegistry;

    public Predicate<String> revisionPredicate(String dataInfoId) {
        Predicate<String> predicate = (revision) -> {

            AppRevisionRegister revisionRegister = appRevisionCacheRegistry.getRevision(revision);
            if (revisionRegister == null) {
                return false;
            }
            if (!revisionRegister.getInterfaces().containsKey(dataInfoId)) {
                return false;
            }
            return true;
        };
        return predicate;
    }
}