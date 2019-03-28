package com.alipay.sofa.configuration; /**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */

import com.alipay.sofa.configuration.impl.JsonConfigurationFile;
import com.alipay.sofa.configuration.model.ConfigFileChangeEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author lepdou
 * @version $Id: JsonConfigurationFileTest.java, v 0.1 2019年03月27日 下午2:58 lepdou Exp $
 */
public class JsonConfigurationFileTest {

    @Test
    public void testNormalFunc() throws InterruptedException {
        String json = "{\"name\": \"test\"}";
        JsonConfigurationFile<TestBean> file = new JsonConfigurationFile<TestBean>(json, TestBean.class);
        final AtomicInteger counter = new AtomicInteger(0);
        file.addChangeListener(new ConfigFileChangeListener() {
            public void onChange(ConfigFileChangeEvent changeEvent) {
                counter.incrementAndGet();
            }
        });

        TestBean bean = file.getObject();
        Assert.assertEquals("test", bean.name);

        String json2 = "{\"name\": \"test2\"}";
        file.updateJsonStr(json2, TestBean.class);

        TimeUnit.MILLISECONDS.sleep(500);

        Assert.assertEquals(1, counter.get());
        Assert.assertEquals("test2", file.getObject().name);
    }

    class TestBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}