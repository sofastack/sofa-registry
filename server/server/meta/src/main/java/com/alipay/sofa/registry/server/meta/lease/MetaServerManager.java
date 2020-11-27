package com.alipay.sofa.registry.server.meta.lease;

import com.alipay.sofa.registry.lifecycle.Lifecycle;
import com.alipay.sofa.registry.server.meta.MetaServer;

/**
 * @author chen.zhu
 * <p>
 * Nov 19, 2020
 */
public interface MetaServerManager extends Lifecycle {

    /**
     * Gets get or create.
     *
     * @param dc the dc
     * @return the get or create
     */
    MetaServer getOrCreate(String dc);

    /**
     * Remove.
     *
     * @param dc the dc
     */
    void remove(String dc);
}
