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
        Map<String, LeaderLockDomain> map = null;
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        try{
            map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        }catch (NullPointerException e){
            LOG.info("DISTRIBUTE-LOCk RheaKV is empty");
        }

        LeaderLockDomain leaderInfo = map.get(defaultCommonConfig.getClusterId());

        if(leaderInfo==null){
            return LeaderInfo.HAS_NO_LEADER;
        }

        return new LeaderInfo(leaderInfo.getEpoch(),leaderInfo.getLeader(),leaderInfo.getExpireTimestamp());
    }
    
    @Override
    protected LeaderInfo doElect() {
        byte[] bytes = rheaKVStore.bGet(distributeLock);
        Map<String,LeaderLockDomain> map=null;
        LeaderLockDomain leaderLockInfo=null;
        try {
            map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
            leaderLockInfo = map.get(defaultCommonConfig.getClusterId());
        }catch (NullPointerException e){
            LOG.info("DISTRIBUTE_LOCk RheaKV is empty");
        }
        
        if(leaderLockInfo==null){
            return competeLeader(defaultCommonConfig.getClusterId());
        }

        ElectorRole role = amILeader(leaderLockInfo.getLeader()) ? ElectorRole.LEADER : ElectorRole.FOLLOWER;

        if(role==ElectorRole.LEADER){
            //主节点
            //String leader = leaderLockInfo.getLeaderInfo().getLeader();
            leaderLockInfo=onLeaderWorking(leaderLockInfo,myself());
        }else{
            //子节点
            leaderLockInfo=onFollowWorking(leaderLockInfo,myself());
        }

        LeaderInfo result = leaderFrom(leaderLockInfo.getOwner(),leaderLockInfo.getEpoch(),leaderLockInfo.getGmtModified(),leaderLockInfo.getDuration());
        //更新leaderInfo并重新插入rheakv
        leaderLockInfo.setEpoch(result.getEpoch());
        leaderLockInfo.setLeader(result.getLeader());
        leaderLockInfo.setExpireTimestamp(result.getExpireTimestamp());
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
        Map<String, LeaderLockDomain> map = null;
        //获取全部Lock存储
        try{
            map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        }catch (NullPointerException e){
            LOG.info("DISTRIBUTE_LOCk RheaKV is empty");
        }
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
        Map<String, LeaderLockDomain> map = null;
        try {
           map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
        }catch (NullPointerException e){
            LOG.info("DISTRIBUTE_LOCk RheaKV is empty");
        }

        if(date>lock.getExpireTimestamp()){
            if (LOG.isInfoEnabled()) {
                LOG.info("lock expire: {}, meta elector start: {}", lock, myself);
            }
            LeaderLockDomain leaderLockInfo = map.get(lock.getDataCenter());
            leaderLockInfo.setGmtModified(new Date());
            leaderLockInfo.setOwner(myself);
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
        Map<String, LeaderLockDomain> map=null;
        LeaderLockDomain leaderLockInfo=null;
        LeaderInfo leaderInfo=null;
        //获取全部Lock存储
        try {
            map = CommandCodec.decodeCommand(bytes, leaderInfoMap.getClass());
            leaderLockInfo = map.get(dataCenter);
            //遍历distribute_lock查看,是否存在"META-MASTER"
            for(Map.Entry<String, LeaderLockDomain> entry: map.entrySet()){
                LeaderLockDomain value = entry.getValue();
                if(value.getLockName()==lockName){
                    if (LOG.isInfoEnabled()) {
                        LOG.info("meta: {} compete error, leader is: {}.", myself(), value.getLeader());
                    }
                    return new LeaderInfo(value.getEpoch(),value.getLeader(),value.getExpireTimestamp());
                }
            }
        }catch (NullPointerException e){
            LOG.info("DISTRIBUTE_LOCk RheaKV is empty");
        }

        if(leaderLockInfo==null){
            //创建Lock
            leaderInfo = leaderFrom(myself(), System.currentTimeMillis(), date, metaElectorConfig.getLockExpireDuration());
            leaderLockInfo=new LeaderLockDomain(lockName,
                    leaderInfo.getEpoch(),
                    leaderInfo.getLeader(),
                    leaderInfo.getExpireTimestamp(),
                    date,
                    dataCenter,myself(),
                    metaElectorConfig.getLockExpireDuration());

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
            leaderInfo = leaderFrom(leaderLockInfo.getLeader(),
                    leaderLockInfo.getEpoch(),
                    date,
                    metaElectorConfig.getLockExpireDuration());

            leaderLockInfo.setExpireTimestamp(leaderInfo.getExpireTimestamp());

            leaderInfoMap.put(dataCenter,leaderLockInfo);
            rheaKVStore.bPut(distributeLock,CommandCodec.encodeCommand(leaderInfoMap));
            if (LOG.isInfoEnabled()) {
                LOG.info("meta: {} compete success, become leader.", myself());
            }
        }
        return leaderInfo;
    }


}
