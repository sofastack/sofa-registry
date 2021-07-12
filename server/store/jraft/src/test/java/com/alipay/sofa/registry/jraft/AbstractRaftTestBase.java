package com.alipay.sofa.registry.jraft;

import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * <h3>registry-parent</h3>
 * <p></p>
 *
 * @author : xingpeng
 * @date : 2021-06-21 21:27
 **/
@ActiveProfiles("test")
@SpringBootTest(classes = AbstractRaftTestBase.RaftTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AbstractRaftTestBase extends AbstractTest implements ApplicationContextAware {
    protected ApplicationContext applicationContext;



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @SpringBootApplication()
    public static class RaftTestConfig {}
}
