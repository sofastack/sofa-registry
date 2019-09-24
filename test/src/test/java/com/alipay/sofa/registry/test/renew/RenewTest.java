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
package com.alipay.sofa.registry.test.renew;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;
import static com.alipay.sofa.registry.common.model.constants.ValueConstants.DEFAULT_INSTANCE_ID;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.test.BaseIntegrationTest;

/**
 * @author kezhu.wukz
 */
@RunWith(SpringRunner.class)
public class RenewTest extends BaseIntegrationTest {

    /**
     * clean datum -> renew
     */
    @Test
    public void testRenewAfterClean() throws InterruptedException {
        String dataId = "test-dataId-" + System.currentTimeMillis();
        String value = "test publish";

        // pub/sub
        {
            PublisherRegistration registration = new PublisherRegistration(dataId);
            registryClient1.register(registration, value);

            SubscriberRegistration subReg = new SubscriberRegistration(dataId,
                new MySubscriberDataObserver());
            subReg.setScopeEnum(ScopeEnum.dataCenter);

            registryClient1.register(subReg);
        }

        Thread.sleep(5000L);

        // check sub
        {
            assertEquals(dataId, this.dataId);
            assertEquals(LOCAL_REGION, userData.getLocalZone());
            assertEquals(1, userData.getZoneData().size());
            assertEquals(1, userData.getZoneData().values().size());
            assertEquals(true, userData.getZoneData().containsKey(LOCAL_REGION));
            assertEquals(1, userData.getZoneData().get(LOCAL_REGION).size());
            assertEquals(value, userData.getZoneData().get(LOCAL_REGION).get(0));
        }

        //clean datum and unsub
        {
            //pub again to ignore renew for 10sec
            PublisherRegistration registration = new PublisherRegistration(dataId);
            registryClient1.register(registration, value);
            //un sub, because will new sub below
            registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
            // sleep to sure pub is done
            Thread.sleep(2000L);
            // clean all datum
            DatumCache datumCache = (DatumCache) dataApplicationContext.getBean("datumCache");
            datumCache.cleanDatum(LOCAL_DATACENTER,
                DataInfo.toDataInfoId(dataId, DEFAULT_INSTANCE_ID, DEFAULT_GROUP));
        }

        // new sub, and got empty datum
        {
            SubscriberRegistration subReg = new SubscriberRegistration(dataId,
                new MySubscriberDataObserver());
            subReg.setScopeEnum(ScopeEnum.dataCenter);
            registryClient1.register(subReg);
        }
        Thread.sleep(3000L);
        assertEquals(0, userData.getZoneData().size());

        //renew in 20sec
        Thread.sleep(20000L);
        assertEquals(1, userData.getZoneData().size());

        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.SUBSCRIBER);
        registryClient1.unregister(dataId, DEFAULT_GROUP, RegistryType.PUBLISHER);
    }

}