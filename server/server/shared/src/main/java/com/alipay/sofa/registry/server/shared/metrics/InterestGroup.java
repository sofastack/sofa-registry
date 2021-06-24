package com.alipay.sofa.registry.server.shared.metrics;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Set;

public class InterestGroup {

    private static final Set<String> interestGroups = Sets.newConcurrentHashSet(Lists.newArrayList(ValueConstants.DEFAULT_GROUP));
    public static void registerInterestGroup(String... groups) {
        interestGroups.addAll(Arrays.asList(groups));
    }

    public static String normalizeGroup(String group){
        if(interestGroups.contains(group)){
            return group;
        }
        return "other";
    }
}
