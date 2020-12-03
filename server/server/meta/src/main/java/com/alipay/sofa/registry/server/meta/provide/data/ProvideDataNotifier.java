package com.alipay.sofa.registry.server.meta.provide.data;

import com.alipay.sofa.registry.common.model.metaserver.ProvideDataChangeEvent;

/**
 * @author chen.zhu
 * <p>
 * Dec 03, 2020
 */
public interface ProvideDataNotifier {

    void notifyProvideDataChange(ProvideDataChangeEvent event);

}
