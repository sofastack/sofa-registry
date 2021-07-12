package com.alipay.sofa.registry.jraft.elector;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.jraft.config.MetaElectorConfig;
import com.alipay.sofa.registry.jraft.domain.LeaderLockDomain;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : xingpeng
 * @date : 2021-06-24 15:20
 **/
public class MetaRaftLeaderElector extends AbstractLeaderElector {

    private static final Logger LOG = LoggerFactory.getLogger(MetaRaftLeaderElector.class);

    @Autowired
    private RheaKVStore rheaKVStore;

    @Autowired
    private MetaElectorConfig metaElectorConfig;

    @Autowired
    private DefaultCommonConfig defaultCommonConfig;

    private static final String distributeLock ="DISTRIBUTE-LOCk";

    private static final String lockName = "META-MASTER";

    /**Map<集群节点,节点锁>*/
    protected Map<String, LeaderLockDomain> leaderInfoMap = new ConcurrentHashMap<>();

    @Override
    protected LeaderInfo doQuery() {
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        Map<String, LeaderLockDomain> map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        LeaderLockDomain leaderInfo = map.get(defaultCommonConfig.getClusterId());

        if(leaderInfo==null){
            return LeaderInfo.HAS_NO_LEADER;
        }

        return leaderInfo.getLeaderInfo();
    }
    
    @Override
    protected LeaderInfo doElect() {
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        Map<String,LeaderLockDomain> map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        LeaderLockDomain leaderLockInfo = map.get(defaultCommonConfig.getClusterId());

        if(leaderLockInfo==null){
            return competeLeader(defaultCommonConfig.getClusterId());
        }

        ElectorRole role = amILeader(leaderLockInfo.getLeaderInfo().getLeader()) ? ElectorRole.LEADER : ElectorRole.FOLLOWER;

        if(role==ElectorRole.LEADER){
            String leader = leaderLockInfo.getLeaderInfo().getLeader();
            leaderLockInfo=onLeaderWorking(leaderLockInfo,myself());
        }else{
            leaderLockInfo=onFollowWorking(leaderLockInfo,myself());
        }

        LeaderInfo result = leaderFrom(leaderLockInfo.getOwner(),leaderLockInfo.getLeaderInfo().getEpoch(),leaderLockInfo.getGmtModified(),leaderLockInfo.getDuration());
        //更新leaderInfo并重新插入rheakv
        leaderLockInfo.setLeaderInfo(result);
        leaderInfoMap.put(leaderLockInfo.getDataCenter(),leaderLockInfo);
        rheaKVStore.bPut(distributeLock,CommandCodec.encodeCommand(leaderInfoMap));

        if (LOG.isInfoEnabled()) {
            LOG.info("meta role : {}, leaderInfo: {}", role, result);
        }
        return result;
    }


    private LeaderLockDomain onLeaderWorking(LeaderLockDomain lock, String myself) {
        Date date=new Date();
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        //获取全部Lock存储
        Map<String, LeaderLockDomain> map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        LeaderLockDomain leaderLockInfo = map.get(lock.getDataCenter());
        if(leaderLockInfo==null){
            LOG.error("leader:{} heartbeat error.", myself);
            return lock;
        }else{
            lock.setGmtModified(date);
            if (LOG.isInfoEnabled()) {
                LOG.info("leader heartbeat: {}", myself);
            }
            return lock;
        }
    }


    public LeaderLockDomain onFollowWorking(LeaderLockDomain lock, String myself) {
        long date=System.currentTimeMillis();
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        Map<String, LeaderLockDomain> map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        if(date>lock.getLeaderInfo().getExpireTimestamp()){
            if (LOG.isInfoEnabled()) {
                LOG.info("lock expire: {}, meta elector start: {}", lock, myself);
            }
            LeaderLockDomain leaderLockInfo = map.get(lock.getDataCenter());
            leaderLockInfo.setGmtModified(new Date());
            leaderLockInfo.setOwner(myself);
//            leaderInfoMap.put(lock.getDataCenter(),leaderLockInfo);
//            rheaKVStore.bPut(lock.getDataCenter(),CommandCodec.encodeCommand(leaderInfoMap));
            if (LOG.isInfoEnabled()) {
                LOG.info("elector finish, new lock: {}", lock);
            }
            return leaderLockInfo;
        }
        return lock;
    }


    static LeaderInfo leaderFrom(String owner, long epoch, Date lastHeartbeat, long duration) {
        return calcLeaderInfo(
                owner,
                epoch,
                lastHeartbeat,
                duration);
    }

    /**
     * compete and return leader
     *
     * @param dataCenter
     * @return
     */
    private LeaderInfo competeLeader(String dataCenter){
        Date date=new Date();
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        //获取全部Lock存储
        Map<String, LeaderLockDomain> map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        LeaderLockDomain leaderLockInfo = map.get(dataCenter);

        //遍历distribute_lock查看,是否存在"META-MASTER"
        for(Map.Entry<String, LeaderLockDomain> entry: map.entrySet()){
            LeaderLockDomain value = entry.getValue();
            if(value.getLockName()==lockName){
                if (LOG.isInfoEnabled()) {
                    LOG.info("meta: {} compete error, leader is: {}.", myself(), value.getLeaderInfo().getLeader());
                }
                return value.getLeaderInfo();
            }
        }

        if(leaderLockInfo==null){
            //创建Lock
            LeaderInfo leaderInfo = leaderFrom(myself(), System.currentTimeMillis(), date, metaElectorConfig.getLockExpireDuration());
            leaderLockInfo=new LeaderLockDomain(lockName,leaderInfo,date,dataCenter,myself(),metaElectorConfig.getLockExpireDuration());

            leaderInfoMap.put(dataCenter,leaderLockInfo);
            rheaKVStore.bPut(distributeLock ,CommandCodec.encodeCommand(leaderInfoMap));

            if (LOG.isInfoEnabled()) {
                LOG.info("meta: {} compete success, become leader.", myself());
            }
        }else {
            //重新定义Lock
            leaderLockInfo.setLockName(lockName);
            leaderLockInfo.setGmtModified(date);
            leaderLockInfo.setDataCenter(dataCenter);
            leaderLockInfo.setLeaderInfo(leaderFrom(leaderLockInfo.getLeaderInfo().getLeader(),
                    leaderLockInfo.getLeaderInfo().getEpoch(),
                    date,
                    metaElectorConfig.getLockExpireDuration()));

            leaderInfoMap.put(dataCenter,leaderLockInfo);
            rheaKVStore.bPut(distributeLock,CommandCodec.encodeCommand(leaderInfoMap));
            if (LOG.isInfoEnabled()) {
                LOG.info("meta: {} compete success, become leader.", myself());
            }
        }
        return leaderLockInfo.getLeaderInfo();
    }


}
