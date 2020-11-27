package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.common.model.Node;

/**
 * @author chen.zhu
 * <p>
 * Nov 19, 2020
 */
public interface LeaseManager<T extends Node> {

    /**
     * Cancel Lease for unpub/unregister perspective.
     *
     * @param renewal the renewal
     * @return the boolean
     */
    boolean cancel(T renewal);

    /**
     * Renew Lease.
     * Return true if the renewal has been existed and renew works
     * Return false if the renewal is a new entry, we have to register it insteadof renew it
     *
     * @param renewal       the renewal
     * @param leaseDuration the lease duration
     * @return the boolean
     */
    boolean renew(T renewal, int leaseDuration);

    /**
     * Evict expired leases.
     * Return true if version should change (some nodes are expired, and removed)
     * Otherwise, return false.
     */
    boolean evict();

}
