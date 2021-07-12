package com.alipay.sofa.registry.jraft.domain;

import com.alipay.sofa.registry.store.api.elector.AbstractLeaderElector;

import java.util.Date;

/**
 * @author : xingpeng
 * @date : 2021-07-12 08:54
 **/
public class LeaderLockDomain {
    private String lockName;

    private String dataCenter;

    private String owner;

    private AbstractLeaderElector.LeaderInfo leaderInfo;

    private Date gmtModified;

    private long duration;

    public LeaderLockDomain(String lockName, AbstractLeaderElector.LeaderInfo leaderInfo, Date gmtModified, String dataCenter, String owner, long duration){
        this.lockName=lockName;
        this.leaderInfo=leaderInfo;
        this.gmtModified=gmtModified;
        this.dataCenter=dataCenter;
        this.owner=owner;
        this.duration=duration;
    }

    public String getLockName() {
        return lockName;
    }

    public AbstractLeaderElector.LeaderInfo getLeaderInfo() {
        return leaderInfo;
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

    public void setLeaderInfo(AbstractLeaderElector.LeaderInfo leaderInfo) {
        this.leaderInfo = leaderInfo;
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

    @Override
    public String toString() {
        return "LeaderLockInfo{" +
                "lockName='" + lockName + '\'' +
                ", dataCenter='" + dataCenter + '\'' +
                ", owner='" + owner + '\'' +
                ", leaderInfo=" + leaderInfo +
                ", gmtModified=" + gmtModified +
                ", duration=" + duration +
                '}';
    }
}
