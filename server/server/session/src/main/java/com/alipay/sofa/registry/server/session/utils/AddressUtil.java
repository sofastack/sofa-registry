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
package com.alipay.sofa.registry.server.session.utils;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author xiaojian.xj
 * @version $Id: AddressUtil.java, v 0.1 2020年12月13日 15:18 xiaojian.xj Exp $
 */
public class AddressUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddressUtil.class);

    //public static String buildURL(String address, Map<String, Collection<String>> params) {
    //    if (CollectionUtils.isEmpty(params)) {
    //        return address;
    //    }
    //    URIBuilder builder = null;
    //    try {
    //        builder = new URIBuilder(address);
    //
    //        for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
    //            String key = entry.getKey();
    //            for (String value : entry.getValue()) {
    //                builder.addParameter(key, value);
    //
    //            }
    //        }
    //        return builder.build().toString();
    //    } catch (URISyntaxException e) {
    //        LOGGER.error("build url error.", e);
    //        return null;
    //    }
    //}

    public static String buildURL(String address, Map<String, Collection<String>> params) {
        List<String> querys = new ArrayList<>();
        for (Map.Entry<String, Collection<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                querys.add(key + "=" + value);
            }
        }
        String queryStr = String.join("&", querys);
        return address + "?" + queryStr;
    }
}