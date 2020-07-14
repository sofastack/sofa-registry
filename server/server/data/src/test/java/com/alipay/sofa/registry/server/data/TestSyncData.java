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
package com.alipay.sofa.registry.server.data;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.server.data.cache.DatumCache;
import com.alipay.sofa.registry.server.data.change.DataSourceTypeEnum;
import com.alipay.sofa.registry.server.data.datasync.Operator;
import com.alipay.sofa.registry.server.data.datasync.sync.Acceptor;
import com.alipay.sofa.registry.util.DatumVersionUtil;

/**
 *
 * @author shangyu.wh
 * @version $Id: TestSyncData.java, v 0.1 2018-03-08 19:48 shangyu.wh Exp $
 */
public class TestSyncData {

    private static Datum datum1;
    private static Datum datum2;
    private static Datum datum3;
    private static Datum datum4;
    private static Datum datum5;

    @Before
    public void setup() {
        //add
        datum1 = new Datum();
        datum1.setVersion(1234);
        datum1.setDataCenter("DefaultDataCenter");
        datum1.setDataInfoId("11");
        datum1.setInstanceId("1");

        datum2 = new Datum();
        datum2.setVersion(5678);
        datum2.setDataCenter("DefaultDataCenter");
        datum2.setDataInfoId("11");
        datum2.setInstanceId("2");

        datum3 = new Datum();
        datum3.setVersion(1111);
        datum3.setDataCenter("DefaultDataCenter");
        datum3.setDataInfoId("33");
        datum3.setInstanceId("3");

        datum4 = new Datum();
        datum4.setVersion(2222);
        datum4.setDataCenter("zui");
        datum4.setDataInfoId("11");
        datum4.setInstanceId("4");

        datum5 = new Datum();
        datum5.setVersion(9999);
        datum5.setDataCenter("DefaultDataCenter");
        datum5.setDataInfoId("11");
        datum5.setInstanceId("5");
    }

    @Test
    public void testAcceptExpired() throws InterruptedException {
        Acceptor acceptor = new Acceptor(30, "11", "DefaultDataCenter", new DatumCache());

        Operator operator1 = new Operator(DatumVersionUtil.nextId(), 0L, datum1,
            DataSourceTypeEnum.SYNC);
        Thread.sleep(1000);
        Operator operator2 = new Operator(DatumVersionUtil.nextId(), operator1.getVersion(),
            datum2, DataSourceTypeEnum.SYNC);
        Thread.sleep(2500);
        Operator operator5 = new Operator(DatumVersionUtil.nextId(), operator2.getVersion(),
            datum5, DataSourceTypeEnum.SYNC);

        acceptor.appendOperator(operator1);
        acceptor.appendOperator(operator2);
        acceptor.appendOperator(operator5);

        acceptor.checkExpired(3);

        Assert.assertEquals(2, acceptor.getAllOperators().size());
        Assert.assertTrue(acceptor.getLastVersion().equals(operator5.getVersion()));

        Collection<Operator> operators = acceptor.getAllOperators();
        Operator[] ops = operators.toArray(new Operator[operators.size()]);
        Assert.assertTrue(ops[0].getVersion().equals(operator2.getVersion()));
        Assert.assertTrue(ops[1].getVersion().equals(operator5.getVersion()));
    }

}