package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

public final class DataUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataUtils.class);

    private DataUtils() {
    }

    public static <T extends BaseInfo> Map<String, Map<String, Map<String, Integer>>> countGroupBy(Collection<T> infos) {
        // instanceId/group/app - > count
        Map<String, Map<String, Map<String, Integer>>> counts = Maps.newHashMap();
        for (T info : infos) {
            Map<String, Map<String, Integer>> groupCount = counts.computeIfAbsent(
                    info.getInstanceId(), k -> Maps.newHashMap());
            Map<String, Integer> appCount = groupCount.computeIfAbsent(info.getGroup(),
                    k -> Maps.newHashMap());
            Integer count = appCount.getOrDefault(info.getAppName(), 0);
            appCount.put(info.getAppName(), count += 1);
        }
        return counts;
    }
}
