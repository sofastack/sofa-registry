package com.alipay.sofa.registry.jraft.domain;

import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : xingpeng
 * @date : 2021-07-12 08:54
 **/
public class LeaderLockDomain implements Serializable {
    private long epoch;

    private String leader;

    private long expireTimestamp;

    private String lockName;

    private String dataCenter;

    private String owner;

    private Date gmtModified;

    private long duration;

    public LeaderLockDomain(String lockName, long epoch, String leader, long expireTimestamp, Date gmtModified, String dataCenter, String owner, long duration){
        this.lockName=lockName;
        this.gmtModified=gmtModified;
        this.dataCenter=dataCenter;
        this.owner=owner;
        this.duration=duration;
        this.epoch=epoch;
        this.leader=leader;
        this.expireTimestamp=expireTimestamp;
    }

    public LeaderLockDomain() {
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public String getLeader() {
        return leader;
    }

    public String getLockName() {
        return lockName;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public String getOwner() {
        return owner;
    }

    public long getDuration() {
        return duration;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public long getExpireTimestamp() {
        return expireTimestamp;
    }

    public void setExpireTimestamp(long expireTimestamp) {
        this.expireTimestamp = expireTimestamp;
    }

    @Override
    public String toString() {
        return "LeaderLockDomain{" +
                "epoch=" + epoch +
                ", leader='" + leader + '\'' +
                ", expireTimestamp=" + expireTimestamp +
                ", lockName='" + lockName + '\'' +
                ", dataCenter='" + dataCenter + '\'' +
                ", owner='" + owner + '\'' +
                ", gmtModified=" + gmtModified +
                ", duration=" + duration +
                '}';
    }
}
