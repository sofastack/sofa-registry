package com.alipay.sofa.registry.jraft.elector;

import com.alipay.sofa.registry.jraft.AbstractRaftTestBase;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

/**
 * @author : xingpeng
 * @date : 2021-06-10 15:30
 **/
public class MetaRaftLeaderElectorTest extends AbstractRaftTestBase {
   private MetaRaftLeaderElector leaderElector;

   private DefaultCommonConfig defaultCommonConfig;

//   @Autowired
//   private RheaKVStore rheaKVStore;

   @Before
   public void beforeMetaRaftLeaderElectorTest(){
       leaderElector = applicationContext.getBean(MetaRaftLeaderElector.class);
       defaultCommonConfig = applicationContext.getBean(DefaultCommonConfig.class);
   }


   @Test
   public void testDoElect() throws TimeoutException,InterruptedException{
       Assert.assertNotNull(leaderElector);
       leaderElector.change2Follow();
       waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);
    }


    
//    @Test
//    public void test(){
//        String str="67789";
//        byte[] bytes = CommandCodec.encodeCommand(str);
//        System.out.println(rheaKVStore.bPut("85867",bytes));
//        rheaKVStore.bPut("85867",CommandCodec.encodeCommand("123"));
//        byte[] bytes1 = rheaKVStore.bGet("85867");
//        System.out.println(CommandCodec.decodeCommand(bytes1, String.class));
//        //System.out.println(defaultCommonConfig.getClusterId());
//    }


}



