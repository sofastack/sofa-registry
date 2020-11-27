package com.alipay.sofa.registry.jraft;

/**
 * @author chen.zhu
 * <p>
 * Nov 21, 2020
 */
public interface LeaderAware {

    /**
     * notify listeners when I'm leader.
     */
    void isLeader();

    /**
     * notify listeners when I'm not leader.
     */
    void notLeader();
}
