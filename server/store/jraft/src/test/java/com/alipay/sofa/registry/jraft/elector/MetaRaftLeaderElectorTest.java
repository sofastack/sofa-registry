package com.alipay.sofa.registry.jraft.elector;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.jraft.AbstractRaftTestBase;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.domain.LeaderLockDomain;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * @author : xingpeng
 * @date : 2021-06-10 15:30
 **/
public class MetaRaftLeaderElectorTest extends AbstractRaftTestBase {

   private MetaRaftLeaderElector leaderElector;

   private DefaultCommonConfig defaultCommonConfig;

   private RheaKVStore rheaKVStore;

   private Map<String, LeaderLockDomain> leaderInfoMap = new ConcurrentHashMap<>();

   @Before
   public void beforeMetaJdbcLeaderElectorTest() {
      leaderElector=applicationContext.getBean(MetaRaftLeaderElector.class);
      defaultCommonConfig=applicationContext.getBean(DefaultCommonConfig.class);
      rheaKVStore=applicationContext.getBean(RheaKVStore.class);
   }

   @Test
   public void testDoElect() throws TimeoutException, InterruptedException {
      Assert.assertNotNull(leaderElector);
      leaderElector.change2Follow();
      waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);
   }

   @Test
   public void testDoQuery() throws TimeoutException, InterruptedException {
      leaderElector.change2Follow();
      waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);
      AbstractLeaderElector.LeaderInfo leaderInfo = leaderElector.doQuery();
      Assert.assertEquals(leaderInfo.getLeader(), leaderElector.myself());
   }

   @Test
   public void testFollowWorking() throws TimeoutException, InterruptedException{
      leaderElector.change2Follow();
      waitConditionUntilTimeOut(() -> leaderElector.amILeader(), 5000);

      byte[] leaderInfoBytes = rheaKVStore.bGet("DISTRIBUTE-LOCk");

      Map<String, LeaderLockDomain> leaderLockDomainMap = CommandCodec.decodeCommand(leaderInfoBytes, leaderInfoMap.getClass());

      LeaderLockDomain leaderLockDomain = leaderLockDomainMap.get(defaultCommonConfig.getClusterId());
      leaderElector.onFollowWorking(leaderLockDomain,leaderElector.myself());
   }

   @Test
   public void testLeaderInfo(){
      Date date = new Date();
      LeaderLockDomain leaderLockDomain=new LeaderLockDomain();
      leaderLockDomain.setOwner("testOwner");
      leaderLockDomain.setGmtModified(date);
      leaderLockDomain.setDuration(1000);
      AbstractLeaderElector.LeaderInfo leaderInfo= MetaRaftLeaderElector.leaderFrom("testOwner",date.getTime(),date,1000);

      Assert.assertEquals(leaderInfo.getLeader(),leaderLockDomain.getOwner());
      Assert.assertEquals(leaderInfo.getEpoch(),leaderLockDomain.getGmtModified().getTime());
      Assert.assertEquals(
              leaderInfo.getExpireTimestamp(),leaderLockDomain.getGmtModified().getTime() + 1000 / 2);
   }
}



