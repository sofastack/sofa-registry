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
package com.alipay.sofa.registry.server.session;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.server.session.utils.DatumUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 @author xuanbei
 @since 2019/2/12
 */
public class DatumUtilsTest {
    @Test
    public void testNewDatumIfNull() {
        Datum datum = new Datum();
        datum.setVersion(19092L);
        datum.setDataId("test-dataId");
        datum.setDataCenter("test-dataCenter");

        Subscriber subscriber = new Subscriber();
        subscriber.setDataId("subscriber-dataId");
        subscriber.setGroup("DEFAULT_GROUP");

        datum = DatumUtils.newDatumIfNull(datum, subscriber);
        Assert.assertEquals(19092L, datum.getVersion());
        Assert.assertEquals("test-dataId", datum.getDataId());
        Assert.assertEquals("test-dataCenter", datum.getDataCenter());
        Assert.assertEquals(null, datum.getGroup());

        datum = DatumUtils.newDatumIfNull(null, subscriber);
        Assert.assertEquals(ValueConstants.DEFAULT_NO_DATUM_VERSION, datum.getVersion());
        Assert.assertEquals("subscriber-dataId", datum.getDataId());
        Assert.assertEquals(ValueConstants.DEFAULT_DATA_CENTER, datum.getDataCenter());
        Assert.assertEquals("DEFAULT_GROUP", datum.getGroup());
    }
}
